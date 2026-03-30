from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlmodel import Session, select

from ..core.security import get_current_user_id
from ..database import get_session
from ..models import Notification

router = APIRouter()


class NotificationReadResponse(BaseModel):
    status: str


@router.get("/list", response_model=list[Notification])
def get_my_notifications(
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    user_id = UUID(current_user_id)
    statement = (
        select(Notification)
        .where(
            Notification.user_id == user_id,
            Notification.is_read == False,
        )
        .order_by(Notification.created_at.desc())
    )
    return session.exec(statement).all()


@router.patch("/{notification_id}/read", response_model=NotificationReadResponse)
def mark_notification_as_read(
    notification_id: UUID,
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    user_id = UUID(current_user_id)
    notification = session.get(Notification, notification_id)
    if not notification:
        raise HTTPException(status_code=404, detail="Notification introuvable")

    if notification.user_id != user_id:
        raise HTTPException(status_code=403, detail="Cette notification ne vous appartient pas")

    if not notification.is_read:
        notification.is_read = True
        session.add(notification)
        session.commit()

    return {"status": "Notification marquee comme lue"}
