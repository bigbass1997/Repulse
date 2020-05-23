[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
### Description
Repulse is a Twitch API wrapper library. It provides a simple to use and consistent structure to access each part of the Twitch API. While the wrapper is not multi-threaded/reactive, it is thread-safe. Meaning that your bot can call on a single instance of the TwitchClient from multiple threads, without issues. Rate-limits are also respected by the wrapper automatically.

### Basic Usage
Reminder: As of April 30, 2020, all Helix endpoints require an access token AND a matching client ID. Also, the client secret is required to refresh the auth token.
```java
public class Example {
	public static void main (String[] args){
		final TwitchClient client = TwitchClientBuilder.builder()
				.withToken("access token here", "refresh token", 0)
				.withClientId("client id")
				.withClientSecret("client secret")
				.withAutoRefresh(true) // will automatically refresh the auth token if it expires
				.build();
		
		// This grabs the game id for Minecraft, and prints all streams of that game.
		List<Game> games = client.helix().getGames(null, "Minecraft");
		client.helix().getStreams(games.get(0).id, null, null, null).forEach(stream -> {
			System.out.println(stream.username + ": " + stream.viewerCount + " " + stream.type + " " + stream.startedAt.toString());
		});
		
		// This creates a new event listener for IRC messages, and then joins two twitch channels.
		client.events().addListener(new Object(){
			@SubscribeEvent
			public void functionNameDoesntMatter(IrcMessageEvent event){
				System.out.println("[#" + event.channel + "][" + event.username + "] " + event.message);
			}
		});
		client.chat().joinChannel("bigbass__");
		client.chat().joinChannel("twitchplayspokemon");
	}
}
```
Ideally you'll want to close the IRC websocket properly, via `client.dispose()` but for this example it isn't necessary. And it would require some kind of loop or hold in the main thread, so that you don't dispose immediately after starting the bot.

### Download
You can go to http://bigbass1997.com:3510/repository/internal/com/lukestadem/repulse/ to see what versions are available.

#### Gradle
```groovy
repositories {
	maven { url = 'http://bigbass1997.com:3510/repository/internal/' }
}
dependencies {
	implementation 'com.lukestadem:repulse:1.0.2'
}
```

#### Maven
```xml
<repository>
	<id>repulse-repo</id>
	<url>http://bigbass1997.com:3510/repository/internal/</url>
</repository>
<dependency>
    <groupId>com.lukestadem</groupId>
    <artifactId>repulse</artifactId>
    <version>1.0.2</version>
</dependency>
```

### Dcumentation
Refer to source javadocs for the bulk of the documentation for now. The TwitchClient class is the main entry point for the entire API, including IRC and PubSub.

This library also comes with a feature to help you watch for when streams go online/offline, which can be accessed via `client.watch()`. Just `.registerChannel(channelName)` for whatever channels you want to recieve events for, and then add a listener for `StreamOnlineEvent` and `StreamOfflineEvent` via `client.events()` similar to the example above.

### Building
Gradle is used as the dependency and build management software. Included in the repo is the Gradle Wrapper which allows you to run gradle commands from the root directory of the project. You can compile and run the program with `gradlew run`. To build the source into a runnable jar, execute `gradlew build`. The resulting jar will be found in `build/libs/*.jar`.

To generate project files for IDEA or Eclipse: `gradlew idea` and `gradlew eclipse` respectively. Your IDE may also have the ability to import Gradle projects.
