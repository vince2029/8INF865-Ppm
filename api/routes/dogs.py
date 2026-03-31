from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel
from sqlmodel import Session, select

from api.core.security import get_current_user_id

from ..database import get_session
from ..models import Dog, Size

router = APIRouter()


class dogCreate(BaseModel):
    name: str
    age: int
    size: Size
    energy_level: int
    is_shy: bool
    owner_id: UUID


class dogInfo(BaseModel):
    id: UUID
    name: str
    age: int
    size: Size
    energy_level: int
    is_shy: bool
    owner_id: UUID


@router.get("/{owner_id}", response_model=dogInfo)
def get_dog_by_owner(
    owner_id: UUID,
    _: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    dog = session.exec(select(Dog).where(Dog.owner_id == owner_id)).first()

    if not dog:
        raise HTTPException(
            status_code=404, detail="Aucun chien trouvé pour cet utilisateur"
        )

    return dog


@router.post("/{owner_id}", response_model=dogInfo, status_code=status.HTTP_201_CREATED)
def create_dog(
    owner_id: UUID,
    dog: dogInfo,
    _: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    newDog = Dog(
        name=dog.name,
        age=dog.age,
        size=dog.size,
        energy_level=dog.energy_level,
        is_shy=dog.is_shy,
        owner_id=owner_id,
    )
    session.add(newDog)
    session.commit()
    session.refresh(newDog)
    return newDog
