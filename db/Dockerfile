FROM postgres:17.2-bookworm
COPY 000.sql /docker-entrypoint-initdb.d/
ARG INIT_SQL_SCRIPT
COPY $INIT_SQL_SCRIPT /docker-entrypoint-initdb.d/001.sql
RUN apt-get update && \
	apt-get install -y sudo man-db curl iputils-ping logrotate tcpdump apt-transport-https libzip4 rsyslog psmisc ntpdate faketime netcat-openbsd vim unzip iproute2 bzip2 wget iptables dirmngr && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
	mkdir -p /etc/postgresql && \
    cat /usr/share/postgresql/postgresql.conf.sample > /etc/postgresql/postgresql.conf && \
    echo "max_prepared_transactions=30" >> /etc/postgresql/postgresql.conf
CMD ["-c", "config_file=/etc/postgresql/postgresql.conf"]
