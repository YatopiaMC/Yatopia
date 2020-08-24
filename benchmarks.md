## Chunkgen benchmarks

| Server type | Minecraft Version | server ver | avg ch/s (jdk.16+12.r7.gb2da6e1a905-1) | avg ch/s (jdk.15+35.r3.gf70fc149b55-1)   |
|-------------|-------------------|------------|----------------------------------------|---|
| Yatopia     | 1.16.1            | d06e04f1f6b1052fc36f5edc5622cbd248c96cb0 | 28.9                                            |   |
| Yatopia     | 1.16.2            | 21c8252193da50ec0faf6252bee8e576715ba355 | 27.8                                            |   |
| Paper       | 1.16.1            | 138                                      | 27.9                                            |   |
| Tuinity     | 1.16.1            | 598986c45a8e69d213ad12eb128129ca2aecc253 |                                                 |   |
| Tuinity     | 1.16.2            | 30ac89a8dbdbf18997caf73484318bc7c81b2f89 |                                                 |   |
| Spigot      |                   |                                          |                                                 |   |
| Akarin      |                   |                                          |                                                 |   |



### How it was tested
This was tested on a raspberry pi 4, 4gb (overclocked to 2ghz), with aikar's flags and 2500MB allocated towards the server. The java version is displayed in the table. The seed is "mojang".