package xyz.voroniyx.deathtotem.config;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ModConfig {

    public boolean EnableTotemConsume = true;
    public boolean TotemConsumeOnlyWhenLastTotemUsed = true;
    public HashSet<PlayerOverrides> PlayerOverrides;

    public ModConfig() {
        PlayerOverrides = new HashSet<>();
    }

    public static String GetConfigPath(Path configDir) {
        return configDir.resolve("death_totem.json").toString();
    }

    public static class PlayerOverrides {

        public PlayerOverrides(UUID playerUUID) {
            PlayerUUID = playerUUID;
        }

        public UUID PlayerUUID;
        public Boolean EnableTotemConsume;
        public Boolean TotemConsumeOnlyWhenLastTotemUsed;
        public String NameOfTriggeringTotem;

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof PlayerOverrides that)) return false;
            return Objects.equals(PlayerUUID, that.PlayerUUID);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(PlayerUUID);
        }

        public boolean IsEmpty() {
            return EnableTotemConsume == null
                    && TotemConsumeOnlyWhenLastTotemUsed == null
                    && NameOfTriggeringTotem == null;
        }
    }

    private Optional<PlayerOverrides> FindOverride(UUID playerUUID) {
        if (PlayerOverrides == null || playerUUID == null) {
            return Optional.empty();
        }
        return PlayerOverrides.stream()
                .filter(x -> playerUUID.equals(x.PlayerUUID))
                .findAny();
    }

    public boolean HasActiveEnableTotemConsumeOverrideThatIsTrue(UUID playerUUID) {
        return FindOverride(playerUUID)
                .map(x -> x.EnableTotemConsume)
                .orElse(false);
    }

    public Boolean GetTotemConsumeOnlyWhenLastTotemUsedOverride(UUID playerUUID) {
        return FindOverride(playerUUID)
                .map(x -> x.TotemConsumeOnlyWhenLastTotemUsed)
                .orElse(null);
    }

    public String GetNameOfTriggeringTotemOverride(UUID playerUUID) {
        return FindOverride(playerUUID)
                .map(x -> x.NameOfTriggeringTotem)
                .orElse(null);
    }

    public boolean AddOrUpdatePlayerOverride(UUID playerUUID, String option, Object newValue) {
        if (playerUUID == null || option == null) {
            return false;
        }
        if (PlayerOverrides == null) {
            PlayerOverrides = new HashSet<>();
        }

        PlayerOverrides config = FindOverride(playerUUID).orElse(null);
        boolean isNew = (config == null);
        if (isNew) {
            config = new PlayerOverrides(playerUUID);
        }

        boolean updated = false;

        switch (option) {
            case "EnableTotemConsume": {
                if (newValue instanceof Boolean value) {
                    config.EnableTotemConsume = value;
                    updated = true;
                }
                break;
            }
            case "TotemConsumeOnlyWhenLastTotemUsed": {
                if (newValue instanceof Boolean value) {
                    config.TotemConsumeOnlyWhenLastTotemUsed = value;
                    updated = true;
                }
                break;
            }
            case "NameOfTriggeringTotem": {
                if (newValue instanceof String value) {
                    config.NameOfTriggeringTotem = value.isBlank() ? null : value;
                    updated = true;
                }
                break;
            }
            default: {
            }
        }

        if (updated && isNew) {
            PlayerOverrides.add(config);
        }

        return updated;
    }

//    public boolean RemovePlayerOverride(UUID playerUUID, String option) {
//        PlayerOverrides config = FindOverride(playerUUID).orElse(null);
//        if (config == null) {
//            return false;
//        }
//
//        switch (option) {
//            case "EnableTotemConsume" -> config.EnableTotemConsume = null;
//            case "TotemConsumeOnlyWhenLastTotemUsed" -> config.TotemConsumeOnlyWhenLastTotemUsed = null;
//            case "NameOfTriggeringTotem" -> config.NameOfTriggeringTotem = null;
//            default -> {
//                return false;
//            }
//        }
//
//        if (config.IsEmpty()) {
//            PlayerOverrides.remove(config);
//        }
//
//        return true;
//    }

//    public int NormalizePlayerOverrides() {
//        if (PlayerOverrides == null) {
//            PlayerOverrides = new HashSet<>();
//            return 0;
//        }
//
//        List<PlayerOverrides> all = new ArrayList<>(PlayerOverrides);
//        HashSet<PlayerOverrides> merged = new HashSet<>();
//        int removed = 0;
//
//        for (PlayerOverrides candidate : all) {
//            if (candidate == null || candidate.PlayerUUID == null) {
//                removed++;
//                continue;
//            }
//
//            PlayerOverrides existing = merged.stream()
//                    .filter(x -> candidate.PlayerUUID.equals(x.PlayerUUID))
//                    .findAny()
//                    .orElse(null);
//
//            if (existing == null) {
//                merged.add(candidate);
//                continue;
//            }
//
//            if (existing.EnableTotemConsume == null) {
//                existing.EnableTotemConsume = candidate.EnableTotemConsume;
//            }
//            if (existing.TotemConsumeOnlyWhenLastTotemUsed == null) {
//                existing.TotemConsumeOnlyWhenLastTotemUsed = candidate.TotemConsumeOnlyWhenLastTotemUsed;
//            }
//            if (existing.NameOfTriggeringTotem == null) {
//                existing.NameOfTriggeringTotem = candidate.NameOfTriggeringTotem;
//            }
//            removed++;
//        }
//        merged.removeIf(PlayerOverrides::IsEmpty);
//
//        PlayerOverrides = merged;
//        return removed;
//    }
}