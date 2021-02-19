/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.driver.internal;

import java.util.Map;

import org.neo4j.driver.Record;
import org.neo4j.driver.Statement;
import org.neo4j.driver.StatementResult;
import org.neo4j.driver.StatementRunner;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.internal.util.Extract;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.types.TypeSystem;

public abstract class AbstractStatementRunner implements StatementRunner
{
    @Override
    public final StatementResult run( String statementTemplate, Value parameters )
    {
        return run( new Statement( statementTemplate, parameters ) );
    }

    @Override
    public final StatementResult run( String statementTemplate, Map<String,Object> statementParameters )
    {
        return run( statementTemplate, parameters( statementParameters ) );
    }

    @Override
    public final StatementResult run( String statementTemplate, Record statementParameters )
    {
        return run( statementTemplate, parameters( statementParameters ) );
    }

    @Override
    public final StatementResult run( String statementText )
    {
        return run( statementText, Values.EmptyMap );
    }

    public static Value parameters( Record record )
    {
        return record == null ? Values.EmptyMap : parameters( record.asMap() );
    }

    public static Value parameters( Map<String,Object> map )
    {
        if ( map == null || map.isEmpty() )
        {
            return Values.EmptyMap;
        }
        return new MapValue( Extract.mapOfValues( map ) );
    }
}
