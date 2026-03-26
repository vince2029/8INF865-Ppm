from sqlmodel import SQLModel, Field, Relationship
from typing import Optional, List
from datetime import datetime
from uuid import uuid4, UUID
from enum import Enum

class Role(str, Enum):
    PARTICULIER = "PARTICULIER"
    PRO = "PRO"

class Size(str, Enum):
    PETIT = "PETIT"
    MOYEN = "MOYEN"
    GRAND = "GRAND"


class ParticipationStatus(str, Enum):
    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    REJECTED = "REJECTED"


class NotificationType(str, Enum):
    REQUEST = "REQUEST"
    INFO = "INFO"

class User(SQLModel, table=True):
    id: UUID = Field(default_factory=uuid4, primary_key=True)
    email: str = Field(unique=True, index=True)
    pseudo: str = Field(unique=True, index=True)
    password: str
    role: Role = Field(default=Role.PARTICULIER)
    points_balance: int = Field(default=0)
    
    dogs: List["Dog"] = Relationship(back_populates="owner")

class Dog(SQLModel, table=True):
    id: UUID = Field(default_factory=uuid4, primary_key=True)
    name: str
    age: int
    size: Size
    energy_level: int = Field(ge=1, le=5)
    is_shy: bool = Field(default=False)
    owner_id: UUID = Field(foreign_key="user.id")
    
    owner: User = Relationship(back_populates="dogs")

class Activity(SQLModel, table=True):
    id: UUID = Field(default_factory=uuid4, primary_key=True)
    creator_id: UUID = Field(foreign_key="user.id")
    title: str
    description: str
    location_name: str
    date_time: datetime
    max_participants: int = Field(default=5)
    
    # Critères de compatibilité des chiens
    min_energy_level: int = Field(default=1, ge=1, le=5)
    max_energy_level: int = Field(default=5, ge=1, le=5)
    allow_shy_dogs: bool = Field(default=True)
    min_dog_size: Size = Field(default=Size.PETIT)
    max_dog_size: Size = Field(default=Size.GRAND)


class Participation(SQLModel, table=True):
    id: UUID = Field(default_factory=uuid4, primary_key=True)
    user_id: UUID = Field(foreign_key="user.id", index=True)
    activity_id: UUID = Field(foreign_key="activity.id", index=True)
    status: ParticipationStatus = Field(default=ParticipationStatus.PENDING)
    created_at: datetime = Field(default_factory=datetime.utcnow)


class ParticipationRequest(SQLModel, table=True):
    id: UUID = Field(default_factory=uuid4, primary_key=True)
    user_id: UUID = Field(foreign_key="user.id", index=True)
    activity_id: UUID = Field(foreign_key="activity.id", index=True)
    status: ParticipationStatus = Field(default=ParticipationStatus.PENDING)
    created_at: datetime = Field(default_factory=datetime.utcnow)


class Notification(SQLModel, table=True):
    id: UUID = Field(default_factory=uuid4, primary_key=True)
    user_id: UUID = Field(foreign_key="user.id", index=True)
    type: NotificationType = Field(default=NotificationType.INFO)
    content: str
    related_activity_id: Optional[UUID] = Field(default=None, foreign_key="activity.id")
    is_read: bool = Field(default=False)
    created_at: datetime = Field(default_factory=datetime.utcnow)