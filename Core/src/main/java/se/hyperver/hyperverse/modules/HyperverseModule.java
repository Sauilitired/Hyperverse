//
// Hyperverse - A Minecraft world management plugin
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package se.hyperver.hyperverse.modules;

import cloud.commandframework.services.ServicePipeline;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.configuration.FileHyperConfiguration;
import se.hyperver.hyperverse.configuration.HyperConfiguration;
import se.hyperver.hyperverse.database.HyperDatabase;
import se.hyperver.hyperverse.database.SQLiteDatabase;
import se.hyperver.hyperverse.flags.FlagContainer;
import se.hyperver.hyperverse.flags.GlobalWorldFlagContainer;
import se.hyperver.hyperverse.flags.WorldFlagContainer;
import se.hyperver.hyperverse.teleportation.SimpleTeleportationManager;
import se.hyperver.hyperverse.teleportation.TeleportationManager;
import se.hyperver.hyperverse.util.HyperConfigShouldGroupProfiles;
import se.hyperver.hyperverse.util.NMS;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.HyperWorldCreator;
import se.hyperver.hyperverse.world.SimpleWorld;
import se.hyperver.hyperverse.world.SimpleWorldManager;
import se.hyperver.hyperverse.world.WorldManager;

public class HyperverseModule extends AbstractModule {

    private static final @NonNull String CRAFTSERVER_CLASS_NAME = Bukkit.getServer().getClass().getName();
    private static final @NonNull String PACKAGE_VERSION =
        CRAFTSERVER_CLASS_NAME.substring("org.bukkit.craftbukkit.".length(),
            CRAFTSERVER_CLASS_NAME.indexOf('.', "org.bukkit.craftbukkit.".length()));

    @Override protected void configure() {
        // Resolve the NMS implementation
        try {
            bind(NMS.class)
                .to((Class<? extends NMS>) Class.forName("se.hyperver.hyperverse.spigotnms." + PACKAGE_VERSION + ".NMSImpl"))
                .in(Singleton.class);
        } catch (final ClassNotFoundException ex) {
            throw new RuntimeException("Server version unsupported", ex);
        }
        bind(Hyperverse.class).toInstance(Hyperverse.getPlugin(Hyperverse.class));
        bind(HyperDatabase.class).to(SQLiteDatabase.class).in(Singleton.class);
        bind(HyperConfiguration.class).to(FileHyperConfiguration.class).in(Singleton.class);
        bind(WorldManager.class).to(SimpleWorldManager.class).in(Singleton.class);
        bind(GlobalWorldFlagContainer.class).toInstance(new GlobalWorldFlagContainer());
        bind(ServicePipeline.class).toInstance(Hyperverse.getApi().getServicePipeline());
        install(new FactoryModuleBuilder().implement(WorldCreator.class, HyperWorldCreator.class)
            .build(HyperWorldCreatorFactory.class));
        install(new FactoryModuleBuilder().implement(HyperWorld.class, SimpleWorld.class)
            .build(HyperWorldFactory.class));
        install(new FactoryModuleBuilder().implement(FlagContainer.class, WorldFlagContainer.class)
            .build(FlagContainerFactory.class));
        install(new FactoryModuleBuilder().implement(TeleportationManager.class,
            SimpleTeleportationManager.class).build(TeleportationManagerFactory.class));
    }

    @Provides
    @HyperConfigShouldGroupProfiles
    boolean shouldGroupProfiles(final @NonNull HyperConfiguration configuration) {
        return configuration.shouldGroupProfiles();
    }
}
