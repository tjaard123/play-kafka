FROM landoop/fast-data-dev

# Add KSQL
ARG REGISTRY_VERSION=5.0.1-lkd-r0
ARG REGISTRY_URL="${ARCHIVE_SERVER}/lkd/packages/schema-registry/schema-registry-${REGISTRY_VERSION}.tar.gz"
RUN wget $DEVARCH_USER $DEVARCH_PASS "$REGISTRY_URL" -O /opt/registry.tar.gz \
    && tar --no-same-owner -xzf /opt/registry.tar.gz -C /opt/ \
    && rm -rf /opt/registry.tar.gz