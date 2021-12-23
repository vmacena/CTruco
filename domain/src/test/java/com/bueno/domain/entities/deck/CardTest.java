/*
 *  Copyright (C) 2021 Lucas B. R. de Oliveira - IFSP/SCL
 *  Contact: lucas <dot> oliveira <at> ifsp <dot> edu <dot> br
 *
 *  This file is part of CTruco (Truco game for didactic purpose).
 *
 *  CTruco is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CTruco is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CTruco.  If not, see <https://www.gnu.org/licenses/>
 */

package com.bueno.domain.entities.deck;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Nested
    @DisplayName("Should allow")
    class ShouldAllow{
        @Test
        @DisplayName("Creating a card with valid rank and suit")
        void creatingCardWithValidRankAndSuit(){
            Card card = Card.of(Rank.SEVEN, Suit.SPADES);
            assertAll(
                    () -> assertEquals(Rank.SEVEN, card.getRank()),
                    () -> assertEquals(Suit.SPADES, card.getSuit())
            );
        }

        @Test
        @DisplayName("creating closed card")
        void creatingClosedCard(){
            Card card = Card.closed();
            assertAll(
                    () -> assertEquals(Rank.HIDDEN, card.getRank()),
                    () -> assertEquals(Suit.HIDDEN, card.getSuit())
            );
        }

        @Test
        @DisplayName("creating closed card")
        void creatingClosedCardWithHiddenRankAndSuit(){
            Card card = Card.of(Rank.HIDDEN, Suit.HIDDEN);
            assertAll(
                    () -> assertEquals(Rank.HIDDEN, card.getRank()),
                    () -> assertEquals(Suit.HIDDEN, card.getSuit())
            );
        }

        @Test
        @DisplayName("create get same reference for same values")
        void createGetSameReferenceForSameValues() {
            Card card = Card.of(Rank.SEVEN, Suit.CLUBS);
            Card closedCard = Card.closed();

            assertAll(
                    () -> assertEquals(Card.of(Rank.SEVEN, Suit.CLUBS), card),
                    () -> assertEquals(Card.closed(), closedCard)
            );
        }
    }

    @Nested
    @DisplayName("Should not allow")
    class ShouldNotAllow{
        @Test
        @DisplayName("creating a card with null rank")
        void creatingCardWithNullRank(){
            assertThrows(NullPointerException.class, () -> Card.of(null, Suit.CLUBS));
        }

        @Test
        @DisplayName("creating a card with null suit")
        void creatingCardWithNullSuit(){
            assertThrows(NullPointerException.class, () -> Card.of(Rank.ACE, null));
        }

        @Test
        @DisplayName("creating card with only rank or suit hidden")
        void creatingCardWithOnlyRankOrSuitHidden() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> Card.of(Rank.HIDDEN, Suit.CLUBS)),
                    () -> assertThrows(IllegalArgumentException.class, () -> Card.of(Rank.SEVEN, Suit.HIDDEN))
            );
        }
    }

    @ParameterizedTest(name = "[{index}]: rank {0} and suit {1} = {2}")
    @DisplayName("Should correctly toString() open cards")
    @CsvSource({"SEVEN,DIAMONDS,[7\u2666]", "ACE,HEARTS,[A\u2665]", "QUEEN,CLUBS,[Q\u2663]", "JACK,SPADES,[J\u2660]", "KING,SPADES,[K\u2660]"})
    void shouldCorrectlyToStringOpenCard(Rank rank, Suit suit, String output){
        assertEquals(output, Card.of(rank, suit).toString());
    }

    @Test
    @DisplayName("Should next rank of hidden be hidden")
    void shouldNextRankOfHiddenBeHidden() {
        assertEquals(Rank.HIDDEN, Rank.HIDDEN.next());
    }

    @Test
    @DisplayName("Should next rank of 3 be 4")
    void shouldNextRankOf3Be4() {
        assertEquals(Rank.FOUR, Rank.THREE.next());
    }

    @Test
    @DisplayName("Should next rank of King be Ace")
    void shouldNextRankOfKingBeAce() {
        assertEquals(Rank.ACE, Rank.KING.next());
    }

    @Test
    @DisplayName("Should correctly toString() closed cards")
    void shouldCorrectlyToStringClosedCard() {
        assertEquals("[Xx]", Card.closed().toString());
    }

    @Test
    @DisplayName("Should a closed card worth less than worst possible card")
    void shouldClosedCardWorthLessThanWorstCard() {
        Card worstCard = Card.of(Rank.FOUR, Suit.DIAMONDS);
        Card vira = Card.of(Rank.FOUR, Suit.CLUBS);
        assertEquals(-1, Card.closed().compareValueTo(worstCard, vira));
    }

    @Test
    @DisplayName("Should same cards be equals")
    void shouldSameCardsBeEquals(){
        assertEquals(Card.of(Rank.TWO, Suit.DIAMONDS), Card.of(Rank.TWO, Suit.DIAMONDS));
    }
}