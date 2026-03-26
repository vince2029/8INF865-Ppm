from uuid import UUID

from fastapi import APIRouter, Depends
from sqlmodel import Session, select

from ..core.security import get_current_user_id
from ..database import get_session
from ..models import Notification

router = APIRouter()


@router.get("/", response_model=list[Notification])
def get_my_notifications(
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    user_id = UUID(current_user_id)
    statement = (
        select(Notification)
        .where(Notification.user_id == user_id)
        .order_by(Notification.created_at.desc())
    )
    return session.exec(statement).all()
