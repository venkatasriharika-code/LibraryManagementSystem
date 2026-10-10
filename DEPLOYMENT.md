# LibraryHub deployment

This project uses Java Servlets, JDBC, and MySQL. GitHub Pages cannot run its Java server or connect securely to MySQL. The included Dockerfile builds a Tomcat 9 WAR and is intended for a Docker web host such as Render.

## Run locally in Eclipse

1. Keep your MySQL password in `database/local.properties` (this file is ignored by Git).
2. Import as an Eclipse Dynamic Web Project with Tomcat 9 and the MySQL Connector/J JAR in `WEB-INF/lib`.
3. Run on Server and open `http://localhost:8080/LibraryManagementSystem/`.

## Deploy\n\nThe repository includes `Dockerfile.vercel` for Vercel container deployment. Vercel runs it as a stateless container function; configure `LIBRARY_DB_URL`, `LIBRARY_DB_USER`, and `LIBRARY_DB_PASSWORD` in Vercel project environment variables.\n\n
1. Push this project to a **private** GitHub repository. Do not commit `database/local.properties`, database dumps, or credentials.
2. Create a MySQL database with the `books` table and import the existing catalog and cover images using a database backup tool.
3. In Render, create a Docker Web Service from the GitHub repository and set `LIBRARY_DB_URL`, `LIBRARY_DB_USER`, and `LIBRARY_DB_PASSWORD` in the service's secret environment settings.
4. Open the deployed URL and confirm the book list and details pages work.

The application has no cloud database configured yet. The live Eclipse database at `localhost` is reachable only on this PC; a hosted service needs its own MySQL endpoint and a deliberate catalog migration.
