To create a JAR file for your plugins and include the `MANIFEST.MF` file with custom attributes like `Manifest-Version` and `Plugin-Class`, follow these steps:

### 1. **Set Up Your Plugin Project Structure**

Make sure your project is properly structured. For example:

```
/my-plugin
 ├── src
 │   └── main
 │       └── java
 │           └── com
 │               └── example
 │                   └── plugins
 │                       └── MyPlugin.java
 ├── resources
 │   └── META-INF
 │       └── MANIFEST.MF
 └── build.gradle (or pom.xml for Maven)
```

### 2. **Create the Plugin Class**

The `MyPlugin.java` should implement the `Plugin` interface that your `PluginManager` expects. For example:

```java
package com.example.plugins;

import com.protonmail.landrevillejf.ide.model.Plugin;

public class MyPlugin implements Plugin {
    
    @Override
    public void initialize() {
        System.out.println("MyPlugin initialized!");
    }

    @Override
    public String getName() {
        return "My Plugin";
    }
}
```

### 3. **Create `MANIFEST.MF` File**

Create a `MANIFEST.MF` file inside the `resources/META-INF` directory of your project (or use `src/main/resources/META-INF` if using Gradle/Maven). This file should look like this:

```
Manifest-Version: 1.0
Plugin-Class: com.example.plugins.MyPlugin
```

### 4. **Build the JAR File**

Now, let's package the project into a JAR file. The process depends on whether you’re using **Gradle** or **Maven**.

#### Using **Gradle**

If you're using Gradle, you can customize the manifest file in your `build.gradle`:

```gradle
plugins {
    id 'java'
}

group = 'com.example'
version = '1.0'

jar {
    manifest {
        attributes(
            'Manifest-Version': '1.0',
            'Plugin-Class': 'com.example.plugins.MyPlugin'
        )
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'your:dependencies:here'
}
```

Once the `build.gradle` is set up, run the following command to build the JAR:

```bash
./gradlew clean build
```

The JAR file will be located in `build/libs/my-plugin-1.0.jar`.

#### Using **Maven**

For Maven, you can modify your `pom.xml` to include the manifest details:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-plugin</artifactId>
    <version>1.0</version>
    <packaging>jar</packaging>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Manifest-Version>1.0</Manifest-Version>
                            <Plugin-Class>com.example.plugins.MyPlugin</Plugin-Class>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <!-- Add your dependencies here -->
    </dependencies>
</project>
```

To build the JAR, run the following command:

```bash
mvn clean package
```

The JAR file will be located in `target/my-plugin-1.0.jar`.

### 5. **Check the JAR File**

To verify that the JAR contains the correct `MANIFEST.MF` with your custom attributes, you can inspect the manifest file using the following command:

```bash
jar tf build/libs/my-plugin-1.0.jar  # Gradle
```

or

```bash
jar tf target/my-plugin-1.0.jar  # Maven
```

You should see the `META-INF/MANIFEST.MF` file in the output. You can then inspect the manifest with:

```bash
jar xf build/libs/my-plugin-1.0.jar META-INF/MANIFEST.MF  # Gradle

cat META-INF/MANIFEST.MF
```

The manifest file should contain:

```
Manifest-Version: 1.0
Plugin-Class: com.example.plugins.MyPlugin
```

### 6. **Deploy and Load the Plugin**

Now that your plugin JAR is created, you can deploy it to your `pluginsDir` (the directory that your `PluginManager` loads from). Make sure your directory structure looks like this:

```
/plugins
 └── my-plugin-1.0.jar
```

### 7. **Test the Plugin**

Run your application, and the `PluginManager` should dynamically load the plugin by using the `Plugin-Class` specified in the `MANIFEST.MF` file.

Your logs should show something like:

```
Loaded plugin: My Plugin
```

This indicates that your plugin was successfully loaded, initialized, and registered.

### Example

```java
public class MyPlugin extends AbstractPlugin {
    
    public MyPlugin() {
        super("MyPlugin", "1.0.0", "A sample plugin", "John Doe");
        
        // Configure the plugin
        setCategory("Tools");
        setAuthorEmail("john.doe@example.com");
        setRequiredHostVersion("1.5.0");
        
        // Add dependencies
        addDependency("database-service");
        addDependency("logging-service");
        
        // Add published events
        addPublishedEvent("myplugin.data.processed");
    }
    
    @Override
    public void enable() {
        super.enable(); // Call parent to handle state transitions
        
        // Plugin-specific enabling logic
        log("MyPlugin enabled!");
        
        // Provide a resource
        addProvidedResource("myService", new MyService());
    }
    
    @Override
    public void disable() {
        // Plugin-specific disabling logic
        log("MyPlugin disabled!");
        
        super.disable(); // Call parent to handle state transitions
    }
}
```

---

## Ajouter un panneau dans l’IDE (LEFT/RIGHT/BOTTOM/CENTER)

Le module `plugin-api` fournit un utilitaire `PanelUtil` qui permet à un plugin d’ajouter des panneaux Swing dans l’IDE sans dépendre de `ide-ui`.

### Enum de région

Les emplacements disponibles sont définis dans `ide-core` via l’énumération :

```java
import com.protonmail.landrevillejf.swingide.core.layout.IdePanelRegion;

public enum IdePanelRegion {
    LEFT,
    RIGHT,
    BOTTOM,
    CENTER
}
```

### Utilisation de PanelUtil dans un plugin

```java
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.layout.IdePanelRegion;
import utils.com.protonmail.landrevillejf.ide.plugin.PanelUtil;

import javax.swing.*;
import java.awt.*;

public class MyToolPlugin extends AbstractPlugin {

    private PanelUtil panelUtil;

    @Override
    public void onStart() {
        // Récupérer le bus d’événements applicatif depuis le contexte du plugin
        EventBus eventBus = getContext().getEventBus();

        // Créer l’utilitaire de panneau pour ce plugin
        panelUtil = new PanelUtil(eventBus, getContext().getPluginId());

        // Construire un panneau Swing à afficher dans l’IDE
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Hello from MyToolPlugin"), BorderLayout.CENTER);

        // Ajouter le panneau à droite (RIGHT) de l’IDE de façon synchrone
        boolean ok = panelUtil.addPanelSync("My Tool", null, panel, IdePanelRegion.RIGHT);
        if (!ok) {
            getLogger().warn("Failed to add My Tool panel");
        }
    }
}
```

### Emplacements supportés

Les valeurs `IdePanelRegion` sont mappées ainsi :

- `IdePanelRegion.LEFT` → zone de gauche (explorer / navigation)
- `IdePanelRegion.RIGHT` → zone de droite (tools)
- `IdePanelRegion.BOTTOM` → zone du bas (output / console)
- `IdePanelRegion.CENTER` → zone centrale (si supportée par la version de l’IDE)

### Compatibilité

Pour conserver la compatibilité avec les anciens plugins, `PanelUtil` expose toujours les méthodes basées sur une `String location` :

```java
panelUtil.addPanelSync("My Tool", null, panel, "right");
```

Le nouvel API typé est recommandé :

```java
panelUtil.addPanelSync("My Tool", null, panel, IdePanelRegion.RIGHT);
```

Les plugins ne dépendent que de `plugin-api` et de `ide-core` (pour `EventBus`, `PanelAddRequest` et `IdePanelRegion`) et n’importent pas `ide-ui`, ce qui évite toute dépendance circulaire avec l’IDE.
