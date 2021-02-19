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
package org.neo4j.driver.internal.reactive;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import org.neo4j.driver.Statement;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.internal.async.ExplicitTransaction;
import org.neo4j.driver.internal.cursor.RxStatementResultCursor;
import org.neo4j.driver.internal.util.Futures;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.reactive.RxStatementResult;
import org.neo4j.driver.reactive.RxTransaction;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.neo4j.driver.Values.parameters;

class InternalRxTransactionTest
{
    @Test
    void commitShouldDelegate()
    {
        ExplicitTransaction tx = mock( ExplicitTransaction.class );
        when( tx.commitAsync() ).thenReturn( Futures.completedWithNull() );

        InternalRxTransaction rxTx = new InternalRxTransaction( tx );
        Publisher<Void> publisher = rxTx.commit();
        StepVerifier.create( publisher ).verifyComplete();

        verify( tx ).commitAsync();
    }

    @Test
    void rollbackShouldDelegate()
    {
        ExplicitTransaction tx = mock( ExplicitTransaction.class );
        when( tx.rollbackAsync() ).thenReturn( Futures.completedWithNull() );

        InternalRxTransaction rxTx = new InternalRxTransaction( tx );
        Publisher<Void> publisher = rxTx.rollback();
        StepVerifier.create( publisher ).verifyComplete();

        verify( tx ).rollbackAsync();
    }

    private static Stream<Function<RxTransaction,RxStatementResult>> allTxRunMethods()
    {
        return Stream.of(
                rxSession -> rxSession.run( "RETURN 1" ),
                rxSession -> rxSession.run( "RETURN $x", parameters( "x", 1 ) ),
                rxSession -> rxSession.run( "RETURN $x", singletonMap( "x", 1 ) ),
                rxSession -> rxSession.run( "RETURN $x",
                        new InternalRecord( singletonList( "x" ), new Value[]{new IntegerValue( 1 )} ) ),
                rxSession -> rxSession.run( new Statement( "RETURN $x", parameters( "x", 1 ) ) )
        );
    }

    @ParameterizedTest
    @MethodSource( "allTxRunMethods" )
    void shouldDelegateRun( Function<RxTransaction,RxStatementResult> runReturnOne ) throws Throwable
    {
        // Given
        ExplicitTransaction tx = mock( ExplicitTransaction.class );
        RxStatementResultCursor cursor = mock( RxStatementResultCursor.class );

        // Run succeeded with a cursor
        when( tx.runRx( any( Statement.class ) ) ).thenReturn( completedFuture( cursor ) );
        InternalRxTransaction rxTx = new InternalRxTransaction( tx );

        // When
        RxStatementResult result = runReturnOne.apply( rxTx );
        // Execute the run
        CompletionStage<RxStatementResultCursor> cursorFuture = ((InternalRxStatementResult) result).cursorFutureSupplier().get();

        // Then
        verify( tx ).runRx( any( Statement.class ) );
        assertThat( Futures.getNow( cursorFuture ), equalTo( cursor ) );
    }

    @ParameterizedTest
    @MethodSource( "allTxRunMethods" )
    void shouldMarkTxIfFailedToRun( Function<RxTransaction,RxStatementResult> runReturnOne ) throws Throwable
    {
        // Given
        Throwable error = new RuntimeException( "Hi there" );
        ExplicitTransaction tx = mock( ExplicitTransaction.class );

        // Run failed with error
        when( tx.runRx( any( Statement.class ) ) ).thenReturn( Futures.failedFuture( error ) );
        InternalRxTransaction rxTx = new InternalRxTransaction( tx );

        // When
        RxStatementResult result = runReturnOne.apply( rxTx );
        // Execute the run
        CompletionStage<RxStatementResultCursor> cursorFuture = ((InternalRxStatementResult) result).cursorFutureSupplier().get();

        // Then
        verify( tx ).runRx( any( Statement.class ) );
        RuntimeException t = assertThrows( CompletionException.class, () -> Futures.getNow( cursorFuture ) );
        assertThat( t.getCause(), equalTo( error ) );
        verify( tx ).markTerminated();
    }
}
