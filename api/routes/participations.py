from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session, select
from pydantic import BaseModel
from typing import Literal, List

from ..core.security import get_current_user_id
from ..database import get_session
from ..models import (
    Activity,
    Notification,
    NotificationType,
    Participation,
    ParticipationRequest,
    ParticipationStatus,
    User,
)

router = APIRouter()

class ParticipationRequestResponse(BaseModel):
    status: str


class ParticipationDecisionPayload(BaseModel):
    decision: Literal["ACCEPTED", "REJECTED"]


class ActivityParticipant(BaseModel):
    id: UUID
    pseudo: str


@router.post("/join/{activity_id}", response_model=ParticipationRequestResponse)
def join_activity(activity_id: UUID, current_user_id: str = Depends(get_current_user_id), session: Session = Depends(get_session)):
    user_id = UUID(current_user_id)
    requester = session.get(User, user_id)
    if not requester:
        raise HTTPException(status_code=404, detail="Utilisateur introuvable")

    # 1. Verifier si l'activite existe
    activity = session.get(Activity, activity_id)
    if not activity:
        raise HTTPException(status_code=404, detail="Activite non trouvee")

    if activity.creator_id == user_id:
        raise HTTPException(status_code=400, detail="Le createur ne peut pas rejoindre sa propre activite")

    # Si deja participant, inutile d'envoyer une demande
    existing_participation = session.exec(
        select(Participation).where(
            Participation.activity_id == activity_id,
            Participation.user_id == user_id,
        )
    ).first()
    if existing_participation:
        raise HTTPException(status_code=400, detail="Vous participez deja a cette activite")

    # Evite les doublons de demande pour un meme utilisateur et une meme activite
    existing_request = session.exec(
        select(ParticipationRequest).where(
            ParticipationRequest.activity_id == activity_id,
            ParticipationRequest.user_id == user_id,
            ParticipationRequest.status == ParticipationStatus.PENDING,
        )
    ).first()
    if existing_request:
        raise HTTPException(status_code=400, detail="Vous avez deja une demande en attente pour cette activite")

    # 2. Creer la demande de participation
    new_participation_request = ParticipationRequest(
        user_id=user_id,
        activity_id=activity_id,
        status=ParticipationStatus.PENDING,
    )
    session.add(new_participation_request)

    # 3. Creer la notification pour l'organisateur
    notification = Notification(
        user_id=activity.creator_id,
        type=NotificationType.REQUEST,
        content=f"{requester.pseudo} souhaite rejoindre votre balade : {activity.title}",
        related_activity_id=activity_id,
    )
    session.add(notification)

    session.commit()
    return {"status": "Demande envoyée"}


@router.post("/decide/{request_id}", response_model=ParticipationRequestResponse)
def decide_participation_request(
    request_id: UUID,
    payload: ParticipationDecisionPayload,
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    creator_id = UUID(current_user_id)

    request = session.get(ParticipationRequest, request_id)
    if not request:
        raise HTTPException(status_code=404, detail="Demande introuvable")

    activity = session.get(Activity, request.activity_id)
    if not activity:
        raise HTTPException(status_code=404, detail="Activite non trouvee")

    if activity.creator_id != creator_id:
        raise HTTPException(status_code=403, detail="Seul le createur de l'activite peut traiter la demande")

    if request.status != ParticipationStatus.PENDING:
        raise HTTPException(status_code=400, detail="Cette demande a deja ete traitee")

    if payload.decision == "ACCEPTED":
        existing_participation = session.exec(
            select(Participation).where(
                Participation.activity_id == request.activity_id,
                Participation.user_id == request.user_id,
            )
        ).first()

        if not existing_participation:
            session.add(
                Participation(
                    user_id=request.user_id,
                    activity_id=request.activity_id,
                    status=ParticipationStatus.ACCEPTED,
                )
            )

        request.status = ParticipationStatus.ACCEPTED
        decision_text = "acceptee"
    else:
        request.status = ParticipationStatus.REJECTED
        decision_text = "refusee"

    creator = session.get(User, creator_id)
    creator_pseudo = creator.pseudo if creator else "L'organisateur"

    session.add(
        Notification(
            user_id=request.user_id,
            type=NotificationType.INFO,
            content=f"{creator_pseudo} a {decision_text} votre demande pour la balade '{activity.title}'.",
            related_activity_id=request.activity_id,
        )
    )

    session.add(request)
    session.commit()
    return {"status": f"Demande {decision_text}"}


@router.delete("/leave/{activity_id}", response_model=ParticipationRequestResponse)
def leave_activity(
    activity_id: UUID,
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    user_id = UUID(current_user_id)
    requester = session.get(User, user_id)
    if not requester:
        raise HTTPException(status_code=404, detail="Utilisateur introuvable")

    # 1. Verifier si l'activite existe
    activity = session.get(Activity, activity_id)
    if not activity:
        raise HTTPException(status_code=404, detail="Activite non trouvee")

    # 2. Verifier qu'une participation existe pour l'utilisateur connecte
    participation = session.exec(
        select(Participation).where(
            Participation.activity_id == activity_id,
            Participation.user_id == user_id,
        )
    ).first()
    if not participation:
        raise HTTPException(status_code=404, detail="Participation introuvable")

    # 3. Supprimer la participation
    session.delete(participation)

    # 4. Notifier l'organisateur
    notification = Notification(
        user_id=activity.creator_id,
        type=NotificationType.INFO,
        content=f"{requester.pseudo} a quitte votre balade : {activity.title}",
        related_activity_id=activity_id,
    )
    session.add(notification)

    session.commit()
    return {"status": "Participation retiree"}


@router.get("/{activity_id}/participants", response_model=List[ActivityParticipant])
def list_activity_participants(
    activity_id: UUID,
    _: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    activity = session.get(Activity, activity_id)
    if not activity:
        raise HTTPException(status_code=404, detail="Activite non trouvee")

    participants = session.exec(
        select(User.id, User.pseudo)
        .join(Participation, Participation.user_id == User.id)
        .where(
            Participation.activity_id == activity_id,
            Participation.status == ParticipationStatus.ACCEPTED,
        )
    ).all()

    return [ActivityParticipant(id=user_id, pseudo=pseudo) for user_id, pseudo in participants]
