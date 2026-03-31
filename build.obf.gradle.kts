plugins {
    id("net.fabricmc.fabric-loom-remap")
    `snowy-setup`
}

dependencies {
    mappings(loom.officialMojangMappings())
}