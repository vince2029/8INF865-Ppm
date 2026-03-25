from fastapi import FastAPI
from .database import init_db
from .routes import activities

app = FastAPI(title="Mille Petites Pattes API")

@app.on_event("startup")
def on_startup():
    init_db()

# C'est ici qu'on ajoute les nouveaux modules de routes
app.include_router(activities.router, prefix="/activities", tags=["Activities"])

@app.get("/")
def read_root():
    return {"message": "Bienvenue sur l'API Mille Petites Pattes !"}