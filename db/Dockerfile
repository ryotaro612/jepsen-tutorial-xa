FROM postgres:17.2-bookworm
COPY 000.sql /docker-entrypoint-initdb.d/
ARG INIT_SQL_SCRIPT
COPY $INIT_SQL_SCRIPT /docker-entrypoint-initdb.d/001.sql
RUN  mkdir -p /etc/postgresql && \
     cat /usr/share/postgresql/postgresql.conf.sample > /etc/postgresql/postgresql.conf && \
     echo "max_prepared_transactions=30" >> /etc/postgresql/postgresql.conf
CMD ["-c", "config_file=/etc/postgresql/postgresql.conf"]
