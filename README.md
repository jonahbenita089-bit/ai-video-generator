# Random Joke Generator

The Flask web app includes a random joke generator powered by [JokeAPI](https://jokeapi.dev/).

## Run locally

```bash
pip install -r requirements.txt
python app.py
```

Open `http://localhost:5000` and choose a category. The server proxies requests to JokeAPI so the browser does not need to handle cross-origin requests directly.

The original long-form video endpoints remain available under `/api/generate-video` and `/api/progress`.

## Features

- Random joke API integration
- Category selector
- Copy joke button
- Dark and light theme toggle
- Save favorite jokes locally in the browser
