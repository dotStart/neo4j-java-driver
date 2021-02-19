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
package org.neo4j.driver.internal.async;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.neo4j.driver.Record;
import org.neo4j.driver.Statement;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.async.AsyncStatementRunner;
import org.neo4j.driver.async.StatementResultCursor;

import static org.neo4j.driver.internal.AbstractStatementRunner.parameters;

public abstract class AsyncAbstractStatementRunner implements AsyncStatementRunner
{
    @Override
    public final CompletionStage<StatementResultCursor> runAsync( String statementTemplate, Value parameters )
    {
        return runAsync( new Statement( statementTemplate, parameters ) );
    }

    @Override
    public final CompletionStage<StatementResultCursor> runAsync( String statementTemplate, Map<String,Object> statementParameters )
    {
        return runAsync( statementTemplate, parameters( statementParameters ) );
    }

    @Override
    public final CompletionStage<StatementResultCursor> runAsync( String statementTemplate, Record statementParameters )
    {
        return runAsync( statementTemplate, parameters( statementParameters ) );
    }

    @Override
    public final CompletionStage<StatementResultCursor> runAsync( String statementText )
    {
        return runAsync( statementText, Values.EmptyMap );
    }
}
