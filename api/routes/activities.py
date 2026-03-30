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
)

router = APIRouter()

class ParticipantRequestInfo(BaseModel):
    pseudo: str
    status: str
    user_id: str
    request_id: str

class ActivityWithCreatorPseudo(BaseModel):
    id: UUID
    creator_id: UUID
    creator_pseudo: str
    title: str
    description: str
    location_name: str
    date_time: datetime
    max_participants: int
    min_energy_level: int
    max_energy_level: int
    allow_shy_dogs: bool
    min_dog_size: Size
    max_dog_size: Size
    participant_count: int
    participant_requests: list[ParticipantRequestInfo] = []

class ActivityCreatePayload(BaseModel):
    title: str
    description: str
    location_name: str
    date_time: datetime
    max_participants: int = 5
    min_energy_level: int = 1
    max_energy_level: int = 5
    allow_shy_dogs: bool = True
    min_dog_size: Size = Size.PETIT
    max_dog_size: Size = Size.GRAND


class ActivityUpdatePayload(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    location_name: Optional[str] = None
    date_time: Optional[datetime] = None
    max_participants: Optional[int] = None
    min_energy_level: Optional[int] = None
    max_energy_level: Optional[int] = None
    allow_shy_dogs: Optional[bool] = None
    min_dog_size: Optional[Size] = None
    max_dog_size: Optional[Size] = None


class ActivityActionResponse(BaseModel):
    status: str


def _serialize_activity(
    activity: Activity,
    creator_pseudo: str,
    participant_count: int = 0,
    participant_requests: Optional[List[ParticipantRequestInfo]] = None,
) -> ActivityWithCreatorPseudo:
    requests = participant_requests or []

    return ActivityWithCreatorPseudo(
        id=activity.id,
        creator_id=activity.creator_id,
        creator_pseudo=creator_pseudo,
        title=activity.title,
        description=activity.description,
        location_name=activity.location_name,
        date_time=activity.date_time,
        max_participants=activity.max_participants,
        min_energy_level=activity.min_energy_level,
        max_energy_level=activity.max_energy_level,
        allow_shy_dogs=activity.allow_shy_dogs,
        min_dog_size=activity.min_dog_size,
        max_dog_size=activity.max_dog_size,
        participant_count=participant_count,
        participant_requests=requests,
    )



def _get_accepted_participant_ids(session: Session, activity_id: UUID) -> List[UUID]:
    return session.exec(
        select(Participation.user_id).where(
            Participation.activity_id == activity_id,
            Participation.status == ParticipationStatus.ACCEPTED,
        )
    ).all()


def _get_accepted_participant_count(session: Session, activity_id: UUID) -> int:
    count = session.exec(
        select(func.count(Participation.id)).where(
            Participation.activity_id == activity_id,
            Participation.status == ParticipationStatus.ACCEPTED,
        )
    ).one()
    return int(count or 0)


def _get_accepted_participant_counts(session: Session, activity_ids: List[UUID]) -> dict[UUID, int]:
    if not activity_ids:
        return {}

    rows = session.exec(
        select(Participation.activity_id, func.count(Participation.id))
        .where(
            Participation.activity_id.in_(activity_ids),
            Participation.status == ParticipationStatus.ACCEPTED,
        )
        .group_by(Participation.activity_id)
    ).all()

    return {activity_id: int(count) for activity_id, count in rows}


def _get_activity_participant_requests(
    session: Session,
    activity_id: UUID,
) -> List[ParticipantRequestInfo]:
    rows = session.exec(
        select(User.pseudo, User.id, ParticipationRequest.status, ParticipationRequest.id)
        .join(User, ParticipationRequest.user_id == User.id)
        .where(ParticipationRequest.activity_id == activity_id)
    ).all()

    return [
        ParticipantRequestInfo(pseudo=pseudo, status=status.value, user_id = str(user_id), request = str(request_id))
        for pseudo,  user_id, status, request_id in rows
    ]


def _get_activity_participant_requests_map(
    session: Session,
    activity_ids: List[UUID],
) -> dict[UUID, List[ParticipantRequestInfo]]:
    if not activity_ids:
        return {}

    rows = session.exec(
        select(ParticipationRequest.activity_id, User.pseudo, ParticipationRequest.status, User.id, ParticipationRequest.id)
        .join(User, ParticipationRequest.user_id == User.id)
        .where(ParticipationRequest.activity_id.in_(activity_ids))
    ).all()

    requests_map: dict[UUID, List[ParticipantRequestInfo]] = {activity_id: [] for activity_id in activity_ids}
    for activity_id, pseudo, status, user_id, request_id in rows:
        requests_map.setdefault(activity_id, []).append(
            ParticipantRequestInfo(pseudo=pseudo, status=status.value, user_id= str(user_id), request_id = str(request_id))
        )

    return requests_map


@router.post("/", response_model=ActivityWithCreatorPseudo)
def create_activity(
    payload: ActivityCreatePayload,
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    creator_id = UUID(current_user_id)
    creator = session.get(User, creator_id)
    if not creator:
        raise HTTPException(status_code=404, detail="Utilisateur introuvable")

    activity = Activity(
        creator_id=creator_id,
        title=payload.title,
        description=payload.description,
        location_name=payload.location_name,
        date_time=payload.date_time,
        max_participants=payload.max_participants,
        min_energy_level=payload.min_energy_level,
        max_energy_level=payload.max_energy_level,
        allow_shy_dogs=payload.allow_shy_dogs,
        min_dog_size=payload.min_dog_size,
        max_dog_size=payload.max_dog_size,
    )
    session.add(activity)
    session.commit()
    session.refresh(activity)
    return _serialize_activity(
        activity,
        creator.pseudo,
        participant_count=0,
        participant_requests=[],
    )


@router.patch("/{activity_id}", response_model=ActivityWithCreatorPseudo)
def update_activity(
    activity_id: UUID,
    payload: ActivityUpdatePayload,
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    creator_id = UUID(current_user_id)
    activity = session.get(Activity, activity_id)
    if not activity:
        raise HTTPException(status_code=404, detail="Activité introuvable")

    if activity.creator_id != creator_id:
        raise HTTPException(status_code=403, detail="Seul le créateur peut modifier cette activité")

    update_data = payload.model_dump(exclude_unset=True)
    for field_name, value in update_data.items():
        setattr(activity, field_name, value)

    session.add(activity)

    creator = session.get(User, creator_id)
    creator_pseudo = creator.pseudo if creator else "L'organisateur"
    participant_ids = _get_accepted_participant_ids(session, activity_id)
    for participant_id in participant_ids:
        session.add(
            Notification(
                user_id=participant_id,
                type=NotificationType.OTHER,
                sender_pseudo=creator_pseudo,
                related_activity_id=activity_id,
                related_activity_name=activity.title,
            )
        )

    session.commit()
    session.refresh(activity)
    participant_count = _get_accepted_participant_count(session, activity_id)
    participant_requests = _get_activity_participant_requests(session, activity_id)
    return _serialize_activity(
        activity,
        creator_pseudo,
        participant_count=participant_count,
        participant_requests=participant_requests,
    )


@router.delete("/{activity_id}", response_model=ActivityActionResponse)
def delete_activity(
    activity_id: UUID,
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    creator_id = UUID(current_user_id)
    activity = session.get(Activity, activity_id)
    if not activity:
        raise HTTPException(status_code=404, detail="Activité introuvable")

    if activity.creator_id != creator_id:
        raise HTTPException(status_code=403, detail="Seul le créateur peut supprimer cette activité")

    creator = session.get(User, creator_id)
    creator_pseudo = creator.pseudo if creator else "L'organisateur"
    activity_title = activity.title

    participant_ids = _get_accepted_participant_ids(session, activity_id)

    linked_requests = session.exec(
        select(ParticipationRequest).where(ParticipationRequest.activity_id == activity_id)
    ).all()
    linked_participations = session.exec(
        select(Participation).where(Participation.activity_id == activity_id)
    ).all()
    linked_notifications = session.exec(
        select(Notification).where(Notification.related_activity_id == activity_id)
    ).all()
    linked_request_ids = [request.id for request in linked_requests]
    if linked_request_ids:
        request_linked_notifications = session.exec(
            select(Notification).where(Notification.related_request_id.in_(linked_request_ids))
        ).all()
        existing_notification_ids = {notification.id for notification in linked_notifications}
        for notification in request_linked_notifications:
            if notification.id not in existing_notification_ids:
                linked_notifications.append(notification)

    # Delete dependent rows first to satisfy FK constraints on Notification.related_request_id.
    for notification in linked_notifications:
        session.delete(notification)

    for request in linked_requests:
        session.delete(request)

    for participation in linked_participations:
        session.delete(participation)

    # Ensure DB-level FK dependents are physically deleted before deleting the activity row.
    session.flush()

    session.delete(activity)

    for participant_id in participant_ids:
        session.add(
            Notification(
                user_id=participant_id,
                type=NotificationType.OTHER,
                sender_pseudo=creator_pseudo,
                related_activity_name=activity_title,
                related_activity_id=None,
            )
        )

    session.commit()
    return {"status": "Activité supprimée"}

@router.get("/list", response_model=List[ActivityWithCreatorPseudo])
def list_activities(
    *,
    session: Session = Depends(get_session),
    _: str = Depends(get_current_user_id),
    # --- Filtres de l'activité ---
    location: Optional[str] = Query(None, description="Filtrer par ville ou lieu"),
    min_date: Optional[datetime] = Query(None, description="Date minimale de la balade"),
    
    # --- Filtres liés au chien de l'utilisateur (plages) ---
    dog_min_energy: Optional[int] = Query(None, ge=1, le=5, description="Énergie minimale du chien (1-5)"),
    dog_max_energy: Optional[int] = Query(None, ge=1, le=5, description="Énergie maximale du chien (1-5)"),
    dog_min_size: Optional[Size] = Query(None, description="Taille minimale du chien ('PETIT', 'MOYEN', 'GRAND')"),
    dog_max_size: Optional[Size] = Query(None, description="Taille maximale du chien ('PETIT', 'MOYEN', 'GRAND')"),
    dog_is_shy: Optional[bool] = Query(None, description="Le chien est timide (true/false)"),
    
    # --- Pagination ---
    offset: int = 0,
    limit: int = Query(default=20, le=100),
):
    """
    DESCRIPTION: Récupère les activités filtrées selon les caractéristiques du chien de l'utilisateur.
    HEADERS: Aucun (Public)
    QUERY PARAMS:
        - location: str (ex: 'Paris')
        - min_date: ISO date (ex: '2026-03-25T10:00:00')
        - dog_min_energy: int entre 1 et 5 (énergie minimale du chien)
        - dog_max_energy: int entre 1 et 5 (énergie maximale du chien)
        - dog_min_size: 'PETIT', 'MOYEN' ou 'GRAND' (taille minimale du chien)
        - dog_max_size: 'PETIT', 'MOYEN' ou 'GRAND' (taille maximale du chien)
        - dog_is_shy: bool (true/false)
        - offset/limit: pour la pagination
    
    FILTRAGE: Les activités retournées doivent avoir une plage de critères qui chevauche
    la plage spécifiée par l'utilisateur. Par exemple, si l'utilisateur a un chien avec
    une énergie de 3-5, seules les activités acceptant des chiens avec une énergie
    qui chevauche 3-5 seront retournées.
    """
    
    statement = select(Activity, User.pseudo).join(User, Activity.creator_id == User.id)

    # 1. Filtre Lieu
    if location:
        statement = statement.where(Activity.location_name.contains(location))
    
    # 2. Filtre Date
    if min_date:
        statement = statement.where(Activity.date_time >= min_date)

    # 3. Filtre Niveau d'énergie du chien
    # Les plages se chevauchent si: activity.min <= user_max AND activity.max >= user_min
    if dog_min_energy is not None or dog_max_energy is not None:
        user_min_energy = dog_min_energy if dog_min_energy is not None else 1
        user_max_energy = dog_max_energy if dog_max_energy is not None else 5
        
        statement = statement.where(
            Activity.min_energy_level <= user_max_energy,
            Activity.max_energy_level >= user_min_energy
        )

    # 4. Filtre Taille du chien
    # Les plages de tailles se chevauchent si: activity.min <= user_max AND activity.max >= user_min
    if dog_min_size is not None or dog_max_size is not None:
        user_min_size = dog_min_size if dog_min_size is not None else Size.PETIT
        user_max_size = dog_max_size if dog_max_size is not None else Size.GRAND
        
        statement = statement.where(
            Activity.min_dog_size <= user_max_size,
            Activity.max_dog_size >= user_min_size
        )

    # 5. Filtre Timidité (si le chien est timide, seules les activités acceptant les chiens timides)
    if dog_is_shy is not None:
        if dog_is_shy:
            statement = statement.where(Activity.allow_shy_dogs == True)

    # Application de la pagination et exécution
    activities_with_creator = session.exec(statement.offset(offset).limit(limit)).all()
    activity_ids = [activity.id for activity, _ in activities_with_creator]
    participant_counts = _get_accepted_participant_counts(session, activity_ids)
    participant_requests_map = _get_activity_participant_requests_map(session, activity_ids)

    return [
        _serialize_activity(
            activity,
            creator_pseudo,
            participant_count=participant_counts.get(activity.id, 0),
            participant_requests=participant_requests_map.get(activity.id, []),
        )
        for activity, creator_pseudo in activities_with_creator
    ]


@router.get("/{activity_id}", response_model=ActivityWithCreatorPseudo)
def get_activity_detail(activity_id: str, _: str = Depends(get_current_user_id),session: Session = Depends(get_session)):
    activity_with_creator = session.exec(
        select(Activity, User.pseudo)
        .join(User, Activity.creator_id == User.id)
        .where(Activity.id == activity_id)
    ).first()
    if not activity_with_creator:
        raise HTTPException(status_code=404, detail="Activité introuvable")
    activity, creator_pseudo = activity_with_creator
    participant_count = _get_accepted_participant_count(session, activity.id)
    participant_requests = _get_activity_participant_requests(session, activity.id)
    return _serialize_activity(
        activity,
        creator_pseudo,
        participant_count=participant_count,
        participant_requests=participant_requests,
    )