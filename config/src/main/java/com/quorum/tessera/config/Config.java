package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.KeyConfigurationAdapter;
import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidBase64;
import com.quorum.tessera.config.constraints.ValidKeyConfiguration;
import com.quorum.tessera.config.constraints.ValidKeyVaultConfiguration;
import com.quorum.tessera.config.constraints.ValidPath;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class Config extends ConfigItem {

    @NotNull
    @Valid
    @XmlElement(name = "jdbc", required = true)
    private JdbcConfig jdbcConfig;

    @NotNull
    @Valid
    @XmlElement(name = "serverConfigs", required = true)
    private List<@Valid ServerConfig> serverConfigs = new ArrayList<>();

    @NotNull
    @Size(min = 1, message = "At least 1 peer must be provided")
    @Valid
    @XmlElement(name = "peer", required = true)
    private List<Peer> peers;

    @Valid
    @NotNull
    @XmlElement(required = true)
    @ValidKeyConfiguration
    @ValidKeyVaultConfiguration
    @XmlJavaTypeAdapter(KeyConfigurationAdapter.class)
    private KeyConfiguration keys;

    @NotNull
    @XmlElement(name = "alwaysSendTo", required = true)
    private List<@ValidBase64 String> alwaysSendTo;

    @ValidPath(checkCanCreate = true)
    @NotNull
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path unixSocketFile;

    @XmlAttribute
    private boolean useWhiteList;

    @XmlAttribute
    private boolean disablePeerDiscovery;

    public Config(final JdbcConfig jdbcConfig,
        final List<ServerConfig> serverConfigs,
        final List<Peer> peers,
        final KeyConfiguration keyConfiguration,
        final List<String> alwaysSendTo,
        final Path unixSocketFile,
        final boolean useWhiteList,
        final boolean disablePeerDiscovery) {
        this.jdbcConfig = jdbcConfig;
        this.serverConfigs = Objects.nonNull(serverConfigs) ? serverConfigs : new ArrayList<>();
        this.peers = peers;
        this.keys = keyConfiguration;
        this.alwaysSendTo = alwaysSendTo;
        this.unixSocketFile = unixSocketFile;
        this.useWhiteList = useWhiteList;
        this.disablePeerDiscovery = disablePeerDiscovery;
    }

    private static Config create() {
        return new Config();
    }

    public Config() {

    }

    public JdbcConfig getJdbcConfig() {
        return this.jdbcConfig;
    }

    public List<ServerConfig> getServerConfigs() {
        return this.serverConfigs;
    }

    @XmlElement(name = "server")
    @Deprecated
    public ServerConfig getServerConfig() {
        if (serverConfigs == null) {
            return null;
        }
        if (serverConfigs.size() != 1) {
            return null;
        }
        return serverConfigs.get(0);
    }

    @Deprecated
    public void setServerConfig(ServerConfig serverConfig) {
        if (serverConfigs != null && !serverConfigs.isEmpty()) {
            throw new UnsupportedOperationException("");
        }

        serverConfig.setEnabled(true);
        serverConfig.setApp(AppType.P2P);

        serverConfigs = Arrays.asList(serverConfig);

    }

    @Deprecated
    public Path getUnixSocketFile() {
        return unixSocketFile;
    }

    public List<Peer> getPeers() {
        return Collections.unmodifiableList(peers);
    }

    public KeyConfiguration getKeys() {
        return this.keys;
    }

    public List<String> getAlwaysSendTo() {
        return this.alwaysSendTo;
    }

    public boolean isUseWhiteList() {
        return this.useWhiteList;
    }

    public boolean isDisablePeerDiscovery() {
        return disablePeerDiscovery;
    }

    @XmlTransient
    public void addPeer(Peer peer) {
        this.peers.add(peer);
    }

    public ServerConfig getP2PServerConfig() {
        // TODO need to revisit
        return getServerConfig();
    }

}
