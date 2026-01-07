"""FastAPI entry point for the Green Coin backend.

The main application, data models, and endpoints live in
`green_coin_flow.py`.  This module simply exposes the FastAPI `app`
object so `uvicorn hackathon_backend:app --reload` works as expected.
"""

from green_coin_flow import app  # re-export for uvicorn


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("green_coin_flow:app", host="0.0.0.0", port=8000, reload=True)
