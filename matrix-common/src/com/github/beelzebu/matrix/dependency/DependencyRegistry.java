/*
 * This file is part of coins3
 *
 * Copyright © 2020 Beelzebu
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.beelzebu.matrix.dependency;

/**
 * @author Beelzebu
 */
public final class DependencyRegistry {

    public boolean shouldAutoLoad(Dependency dependency) {
        switch (dependency) {
            case ASM:
            case ASM_COMMONS:
            case JAR_RELOCATOR:
                return false;
            default:
                return true;
        }
    }
}
