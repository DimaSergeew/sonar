/*
 *  Copyright (c) 2023, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.sonar.api.fallback;

import java.util.HashMap;
import java.util.Map;

public interface Fallback {
    Map<String, FallbackConnection> connected = new HashMap<>();

    static boolean connection(final FallbackConnection connection) {
        if (connected.containsKey(connection.getUsername())) {
            return false;
        }

        connected.put(connection.getUsername(), connection);
        return true;
    }
}
