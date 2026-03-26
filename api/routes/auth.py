from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlmodel import Session, select
from pydantic import BaseModel, EmailStr
from typing import Optional
from uuid import UUID

from ..database import get_session
from ..models import User, Role
from ..core.security import hash_password, verify_password, create_access_token

router = APIRouter()

# --- Schémas de données (Pydantic) ---

class UserCreate(BaseModel):
    email: EmailStr
    password: str

class UserResponse(BaseModel):
    id: UUID
    email: str
    role: str
    points_balance: int

    class Config:
        from_attributes = True

class Token(BaseModel):
    access_token: str
    token_type: str
    user_id: str

# --- Routes ---

@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def register(user_data: UserCreate, session: Session = Depends(get_session)):
    """
    Crée un nouvel utilisateur avec un mot de passe haché par bcrypt.
    """
    # 1. Vérifier si l'email est déjà pris
    statement = select(User).where(User.email == user_data.email)
    existing_user = session.exec(statement).first()
    
    if existing_user:
        raise HTTPException(
            status_code=400, 
            detail="Cet email est déjà enregistré."
        )
    
    # 2. Hacher le mot de passe et créer l'entrée
    hashed_pwd = hash_password(user_data.password)
    new_user = User(
        email=user_data.email,
        password=hashed_pwd,
        role=Role.PARTICULIER,
        points_balance=0
    )
    
    session.add(new_user)
    session.commit()
    session.refresh(new_user)
    
    return new_user


@router.post("/login", response_model=Token)
def login(form_data: OAuth2PasswordRequestForm = Depends(), session: Session = Depends(get_session)):
    """
    Vérifie les identifiants et retourne un Access Token JWT.
    Utilise OAuth2PasswordRequestForm avec username (email) et password.
    """
    # 1. Récupérer l'utilisateur par username (traité comme email)
    statement = select(User).where(User.email == form_data.username)
    user = session.exec(statement).first()
    
    # 2. Vérifier l'existence et le mot de passe via bcrypt
    if not user or not verify_password(form_data.password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Email ou mot de passe incorrect",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    # 3. Générer le token JWT
    access_token = create_access_token(
        data={"sub": str(user.id), "role": user.role}
    )
    
    return {
        "access_token": access_token, 
        "token_type": "bearer",
        "user_id": str(user.id)
    }