from fastapi import FastAPI
from .database import init_db
from .routes import auth, activities, participations, notifications

app = FastAPI(title="Mille Petites Pattes API")

@app.on_event("startup")
def on_startup():
    init_db()

app.include_router(auth.router, prefix="/auth", tags=["Auth"])
app.include_router(activities.router, prefix="/activity", tags=["Activities"])
app.include_router(participations.router, prefix="/participation", tags=["Participations"])
app.include_router(notifications.router, prefix="/notification", tags=["Notifications"])
app.include_router(dogs.router, prefix="/dog", tags=["Dogs"])

@app.get("/")
def read_root():
    return {"message": "Bienvenue sur l'API Mille Petites Pattes !"}