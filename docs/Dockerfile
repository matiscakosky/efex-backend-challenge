FROM node:alpine

WORKDIR /app

COPY openapi.yaml .

RUN npm install -g @redocly/cli

EXPOSE 8080

CMD ["npx", "@redocly/cli", "preview-docs", "--host", "0.0.0.0", "openapi.yaml"]
