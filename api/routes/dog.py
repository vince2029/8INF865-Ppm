from api.core.security import get_current_user_id
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlmodel import Session, select
from sqlalchemy import func
from typing import Optional, List
from datetime import datetime
from pydantic import BaseModel
from uuid import UUID
from ..database import get_session
from ..models import (
    Activity,
    Notification,
    NotificationType,
    Participation,
    ParticipationRequest,
    ParticipationStatus,
    Size,
    User,
    Dog
)

router = APIRouter()

class dogInfo(BaseModel):
    id: UUID
    name: str
    age: int
    size: Size
    energy_level: int
    is_shy: bool
    owner_id: UUID

@router.get("/dog/{owner_id}", response_model=dogInfo)
def get_dog_by_owner(
    owner_id: UUID,
    _: str = Depends(get_current_user_id),
    session: Session = Depends(get_session)
):
    dog = session.exec(
        select(Dog).where(Dog.owner_id == owner_id)
    ).first()

    if not dog:
        raise HTTPException(status_code=404, detail="Aucun chien trouvé pour cet utilisateur")

    return dog

