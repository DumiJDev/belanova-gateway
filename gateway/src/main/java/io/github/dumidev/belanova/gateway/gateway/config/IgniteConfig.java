package io.github.dumidev.belanova.gateway.gateway.config;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.ignite.IgniteException;

import java.util.Collections;

@Configuration
public class IgniteConfig {
    
    @Bean
    public Ignite igniteInstance() {
        try {
            IgniteConfiguration cfg = new IgniteConfiguration();
            cfg.setClientMode(true);
            cfg.setIgniteInstanceName("ServiceIgniteClient");
            
            // Configure discovery SPI with more robust settings
            TcpDiscoverySpi spi = new TcpDiscoverySpi();
            TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
            ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
            spi.setIpFinder(ipFinder);
            
            // Add connection timeout
            spi.setSocketTimeout(30000);
            // Add network timeout
            spi.setNetworkTimeout(10000);
            // Add join timeout
            spi.setJoinTimeout(30000);
            
            cfg.setDiscoverySpi(spi);
            
            // Add failure detection timeout
            cfg.setFailureDetectionTimeout(60000);
            
            // Enable peer class loading
            cfg.setPeerClassLoadingEnabled(true);
            
            return Ignition.start(cfg);
        } catch (IgniteException e) {
            throw new IllegalStateException("Failed to initialize Ignite instance: " + e.getMessage(), e);
        }
    }
}