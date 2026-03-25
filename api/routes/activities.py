from fastapi import APIRouter, Depends
from sqlmodel import Session, select
from ..database import get_session
from ..models import Activity

router = APIRouter()

@router.get("/", response_model=list[Activity])
def list_activities(session: Session = Depends(get_session)):
    return session.exec(select(Activity)).all()