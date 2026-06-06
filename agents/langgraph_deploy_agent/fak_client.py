import requests

FAK_URL = "http://localhost:8080/api/v1/validate"

def validate(envelope: dict) -> dict:
    response = requests.post(FAK_URL, json=envelope)
    return response.json()