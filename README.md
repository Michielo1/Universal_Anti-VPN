# Universal_Anti-VPN
Spigot Universal Anti-VPN with multiple backends and automatic fallover management.


## Supported backends
Currently all backends support API keys that you can use in this plugin to exceed the amount of free queries. These API keys should be purchased on their respective platforms and can be set in the config.yml of this plugin. Issues with API keys or false-positives should be handled by the respective backend provider and not this plugin.

- [KauriVPN](https://funkemunky.cc/shop)
- [Proxycheck](https://proxycheck.io/)

### Backend overview

| Backend     | Free queries   | can use API for more queries |
|-------------|----------------|:----------------------------:|
| KauriVPN    |    20000/month |      ✔️                      |
| Proxycheck  |    1000/day    |      ✔️                      |


## Plugin setup
After installing the plugin and having restarted your server, you will find a ``config.yml`` file. Here you can find multiple configurable settings but none are required to be adjusted to work. This means **the plugin can function properly without any configuration whatsoever**.

Below you will find a table of all configurable settings. Please note this omits the values for configuring your H2 or mysql database. If you are not familiar with this, just keep as is to use an H2 database which will work without configuration. We also omited the API's section where you can enable/disable API's and enter API-keys if wanted.

| Section   | Subsection | Setting | Allowed values | Notes |
|-----------|------------|---------|----------------|-------|
| Settings  | VPN        | alert   | true or false  | Whether or not an alert should be sent upon a player attempting to join with a VPN |
| Settings  | VPN        | kick    | true or false  | Whether or not a player attempting to join with a VPN should be kicked from the server |
| Settings  | cache      | enabled | true or false  | Whether or not we should cache values (**highly recommended**) |
| Settings  | cache      | type    | "H2" or "mysql"  | What cache type to use |
| Settings  | cache      | cleanup_interval   | any number  | How often the plugin checks for invalidated cached records in minutes |
| Fallback-protocol  |   | primary   | any supported backend  | What the primary backend is |
| Fallback-protocol  |   | fallback_protocol   | an array of supported backend(s)  | What order the fallback system uses. |

To go a bit more in-depth of the fallback_protocol setting; this setting uses an array of supported backend(s). This means if you want to, for example, use proxycheck as fallback API, this would be ``["proxycheck]"``. If you wanted to first attempt proxycheck and after that kaurivpn, this would be ``["proxycheck", "kaurivpn"]``. Please note this fallback-protocol is only used when the ``primary`` backend fails.


## Commands & Permissions
| Command                 | Permission        | Note              |
|-------------------------|-------------------|-------------------|
| /antivpn whitelist <IP> | antivpn.whitelist | Whitelists the IP |
| /antivpn block <IP>     | antivpn.block     | Blocks the IP     |

## Support
Join our discord for support [here](https://discord.gg/XkDPdEfcQJ).
