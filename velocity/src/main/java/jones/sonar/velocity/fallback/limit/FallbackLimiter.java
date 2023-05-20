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

package jones.sonar.velocity.fallback.limit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jones.sonar.api.fallback.FallbackConnection;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class FallbackLimiter {
    private final Cache<InetAddress, Byte> CHECKS = Caffeine.newBuilder()
            .expireAfterWrite(1L, TimeUnit.MINUTES)
            .build();
    private static final byte LIMIT_PER_MINUTE = 2; // max. 2 checks per minute

    public boolean shouldDeny(final FallbackConnection connection) {

        // Using InetSocketAddress is not good since we don't want the port
        var inetAddress = ((InetSocketAddress) connection.getChannel().remoteAddress()).getAddress();

        if (CHECKS.asMap().containsKey(inetAddress)) {
            final byte newCount = (byte) (CHECKS.asMap().get(inetAddress) + 1);

            CHECKS.asMap().replace(inetAddress, newCount);
            return newCount > 2;
        }

        CHECKS.put(inetAddress, (byte) 1);
        return false;
    }
}
