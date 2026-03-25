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

class User(SQLModel, table=True):
    id: UUID = Field(default_factory=uuid4, primary_key=True)
    email: str = Field(unique=True, index=True)
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