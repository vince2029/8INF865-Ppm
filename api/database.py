import os
from pathlib import Path
import yaml
from passlib.context import CryptContext
from sqlmodel import SQLModel, create_engine, Session, select
from .models import User, Dog, Activity, Role, Size
from datetime import datetime

DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://user:password@db:5432/millepattes")
engine = create_engine(DATABASE_URL)
SEED_FILE_PATH = Path(os.getenv("SEED_FILE_PATH", Path(__file__).with_name("seed_data.yaml")))
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def _load_seed_data() -> dict:
    if not SEED_FILE_PATH.exists():
        return {"users": [], "dogs": [], "activities": []}

    with SEED_FILE_PATH.open("r", encoding="utf-8") as seed_file:
        data = yaml.safe_load(seed_file) or {}

    return {
        "users": data.get("users", []),
        "dogs": data.get("dogs", []),
        "activities": data.get("activities", []),
    }


def _ensure_hashed_password(raw_password: str) -> str:
    # If a bcrypt hash is already provided in YAML, keep it as-is.
    if raw_password.startswith("$2a$") or raw_password.startswith("$2b$") or raw_password.startswith("$2y$"):
        return raw_password
    return pwd_context.hash(raw_password)


def init_db():
    SQLModel.metadata.create_all(engine)

    with Session(engine) as session:
        # Vérifie si on a déjà des données pour éviter les doublons
        if session.exec(select(User)).first():
            return

        seed_data = _load_seed_data()
        users_by_email = {}

        for user_data in seed_data["users"]:
            user = User(
                email=user_data["email"],
                password=_ensure_hashed_password(user_data["password"]),
                role=Role(user_data.get("role", Role.PARTICULIER.value)),
                points_balance=user_data.get("points_balance", 0),
            )
            session.add(user)
            users_by_email[user.email] = user

        session.commit()

        for user in users_by_email.values():
            session.refresh(user)

        for dog_data in seed_data["dogs"]:
            owner = users_by_email.get(dog_data["owner_email"])
            if owner is None:
                continue

            dog = Dog(
                name=dog_data["name"],
                age=dog_data["age"],
                size=Size(dog_data["size"]),
                energy_level=dog_data.get("energy_level", 3),
                is_shy=dog_data.get("is_shy", False),
                owner_id=owner.id,
            )
            session.add(dog)

        for activity_data in seed_data["activities"]:
            creator = users_by_email.get(activity_data["creator_email"])
            if creator is None:
                continue

            activity = Activity(
                creator_id=creator.id,
                title=activity_data["title"],
                description=activity_data["description"],
                location_name=activity_data["location_name"],
                date_time=datetime.fromisoformat(activity_data["date_time"]),
                max_participants=activity_data.get("max_participants", 5),
            )
            session.add(activity)

        session.commit()

def get_session():
    with Session(engine) as session:
        yield session