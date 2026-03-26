from fastapi import APIRouter, Depends, Query, HTTPException
from sqlmodel import Session, select
from typing import Optional, List
from datetime import datetime
from ..database import get_session
from ..models import Activity, Dog, User, Size

router = APIRouter()

@router.get("/")
def list_activities(
    *,
    session: Session = Depends(get_session),
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
    limit: int = Query(default=20, le=100)
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
    
    statement = select(Activity)

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
    activities = session.exec(statement.offset(offset).limit(limit)).all()
    
    return activities


@router.get("/{activity_id}")
def get_activity_detail(activity_id: str, session: Session = Depends(get_session)):
    activity = session.get(Activity, activity_id)
    if not activity:
        raise HTTPException(status_code=404, detail="Activité introuvable")
    return activity