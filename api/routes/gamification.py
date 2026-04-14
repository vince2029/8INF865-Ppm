from datetime import datetime
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlmodel import Session, select

from ..core.security import get_current_user_id
from ..database import get_session
from ..models import Activity, PartnerReward, Participation, ParticipationStatus, User

router = APIRouter()

MONTHLY_ACTIVITY_GOAL = 5
REWARD_ACTIVITY_STEP = 3
POINTS_PER_ACCEPTED_ACTIVITY = 20


class GamificationSummaryResponse(BaseModel):
    points_balance: int
    monthly_activity_count: int
    monthly_activity_goal: int
    monthly_progress_ratio: float
    monthly_message: str
    reward_progress_count: int
    reward_progress_goal: int
    reward_progress_ratio: float
    remaining_activities_for_next_reward: int
    next_reward_message: str
    points_per_accepted_activity: int


class RewardItemResponse(BaseModel):
    id: str
    partner_name: str
    title: str
    description: str
    points_cost: int
    discount_label: str
    is_unlocked: bool
    points_missing: int


@router.get("/summary", response_model=GamificationSummaryResponse)
def get_gamification_summary(
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    user_id = UUID(current_user_id)
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="Utilisateur introuvable")

    now = datetime.utcnow()
    month_start = datetime(now.year, now.month, 1)
    if now.month == 12:
        next_month_start = datetime(now.year + 1, 1, 1)
    else:
        next_month_start = datetime(now.year, now.month + 1, 1)

    created_activity_ids = session.exec(
        select(Activity.id).where(
            Activity.creator_id == user_id,
            Activity.date_time >= month_start,
            Activity.date_time < next_month_start,
        )
    ).all()

    participated_activity_ids = session.exec(
        select(Participation.activity_id)
        .join(Activity, Participation.activity_id == Activity.id)
        .where(
            Participation.user_id == user_id,
            Participation.status == ParticipationStatus.ACCEPTED,
            Activity.date_time >= month_start,
            Activity.date_time < next_month_start,
        )
    ).all()

    monthly_count = len(set(created_activity_ids) | set(participated_activity_ids))
    monthly_ratio = min(monthly_count / MONTHLY_ACTIVITY_GOAL, 1.0)

    total_accepted_participations = session.exec(
        select(Participation).where(
            Participation.user_id == user_id,
            Participation.status == ParticipationStatus.ACCEPTED,
        )
    ).all()
    total_accepted_count = len(total_accepted_participations)

    reward_cycle_progress = total_accepted_count % REWARD_ACTIVITY_STEP
    if total_accepted_count == 0:
        reward_ratio = 0.0
        remaining_for_next_reward = REWARD_ACTIVITY_STEP
    elif reward_cycle_progress == 0:
        reward_ratio = 1.0
        remaining_for_next_reward = 0
    else:
        reward_ratio = reward_cycle_progress / REWARD_ACTIVITY_STEP
        remaining_for_next_reward = REWARD_ACTIVITY_STEP - reward_cycle_progress

    return {
        "points_balance": user.points_balance,
        "monthly_activity_count": monthly_count,
        "monthly_activity_goal": MONTHLY_ACTIVITY_GOAL,
        "monthly_progress_ratio": monthly_ratio,
        "monthly_message": f"{monthly_count}/{MONTHLY_ACTIVITY_GOAL} balades ce mois-ci. Continuez comme ca !",
        "reward_progress_count": reward_cycle_progress,
        "reward_progress_goal": REWARD_ACTIVITY_STEP,
        "reward_progress_ratio": reward_ratio,
        "remaining_activities_for_next_reward": remaining_for_next_reward,
        "next_reward_message": f"{remaining_for_next_reward} balade(s) avant la prochaine recompense",
        "points_per_accepted_activity": POINTS_PER_ACCEPTED_ACTIVITY,
    }


@router.get("/rewards", response_model=list[RewardItemResponse])
def get_rewards(
    current_user_id: str = Depends(get_current_user_id),
    session: Session = Depends(get_session),
):
    user_id = UUID(current_user_id)
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="Utilisateur introuvable")

    rewards = session.exec(
        select(PartnerReward)
        .where(PartnerReward.is_active == True)
        .order_by(PartnerReward.points_cost)
    ).all()

    response = []
    for reward in rewards:
        points_missing = max(reward.points_cost - user.points_balance, 0)
        response.append(
            {
                "id": str(reward.id),
                "partner_name": reward.partner_name,
                "title": reward.title,
                "description": reward.description,
                "points_cost": reward.points_cost,
                "discount_label": reward.discount_label,
                "is_unlocked": points_missing == 0,
                "points_missing": points_missing,
            }
        )

    return response
