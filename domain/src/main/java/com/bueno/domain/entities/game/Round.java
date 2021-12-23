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

package com.bueno.domain.entities.game;

import com.bueno.domain.entities.deck.Card;
import com.bueno.domain.entities.player.util.Player;
import com.bueno.domain.entities.truco.Truco;
import com.bueno.domain.entities.truco.TrucoResult;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class Round {

    private Player firstToPlay;
    private Player lastToPlay;
    private final Card vira;
    private Hand hand;
    private Player winner;
    private Card firstCard;
    private Card lastCard;

    private final static Logger LOGGER = Logger.getLogger(Round.class.getName());

    public Round(Player firstToPlay, Player lastToPlay, Hand hand) {
        this.firstToPlay = Objects.requireNonNull(firstToPlay, "First to play must not be null!");
        this.lastToPlay = Objects.requireNonNull(lastToPlay, "Second to play must not be null!");
        this.hand = Objects.requireNonNull(hand, "Hand must not be null!");
        this.vira = Objects.requireNonNull(hand.getVira(), "Vira must not be null!");
        this.hand.setCardToPlayAgainst(null);
    }

    public Round(Player firstToPlay, Card firstCard, Player lastToPlay, Card lastCard, Card vira) {
        this.firstToPlay = Objects.requireNonNull(firstToPlay, "First to play must not be null!");
        this.lastToPlay = Objects.requireNonNull(lastToPlay, "Second to play must not be null!");
        this.firstCard = Objects.requireNonNull(firstCard, "First card played must not be null!");
        this.lastCard = Objects.requireNonNull(lastCard, "Last card played must not be null!");
        this.vira = Objects.requireNonNull(vira, "Vira must not be null!");
        validateCards();
    }

    public void play2() {
        final Optional<Card> possibleWinnerCard = winnerCard();
        final Player winner = possibleWinnerCard
                .map(card -> card.equals(firstCard)? firstToPlay : lastToPlay)
                .orElse(null);
        this.winner = winner;
    }


    public Optional<Card> winnerCard(){
        if (firstCard.compareValueTo(lastCard, vira) == 0) return Optional.empty();
        return firstCard.compareValueTo(lastCard, vira) > 0 ? Optional.of(firstCard) : Optional.of(lastCard);
    }

    public void play(){
        winner = null;

        if(isAbleToRequestScoreIncrement(firstToPlay)) {
            final boolean hasWinnerByRun = handleTruco(firstToPlay, lastToPlay).isPresent();
            if (hasWinnerByRun) return;
        }

        firstCard = Objects.requireNonNull(firstToPlay.chooseCardToPlay().content(), "First card played must not be null!");
        hand.setCardToPlayAgainst(firstCard);
        hand.addOpenCard(firstCard);
        lastToPlay.handleOpponentPlay();

        if(isAbleToRequestScoreIncrement(lastToPlay)) {
            final boolean hasWinnerByRun = handleTruco(lastToPlay, firstToPlay).isPresent();
            if (hasWinnerByRun) return;
        }

        lastCard = Objects.requireNonNull(lastToPlay.chooseCardToPlay().content(), "Last card played must not be null!");
        hand.setCardToPlayAgainst(null);
        hand.addOpenCard(lastCard);
        firstToPlay.handleOpponentPlay();

        validateCards();
        Optional<Card> highestCard = getHighestCard();

        highestCard.ifPresent(card -> winner = (card.equals(firstCard) ? firstToPlay : lastToPlay));

        LOGGER.info(firstToPlay.getUsername() + ": " + firstCard + " | " + lastToPlay.getUsername() +
                ": " + lastCard + " | Result: " + (winner == null? "Draw" : winner.getUsername()));
    }


    private boolean isAbleToRequestScoreIncrement(Player requester) {
        final Player previousRequester = hand.getLastBetRaiser();
        final boolean isAbleToRequest = previousRequester == null || ! previousRequester.equals(requester);

        LOGGER.info(requester.getUsername() + " is " + (isAbleToRequest? "able" : "not able")
                + " to request to increase hand score. Previous requester: " + previousRequester);

        return isAbleToRequest;
    }

    private Optional<HandResult> handleTruco(Player requester, Player responder) {
        final Truco truco = new Truco(requester, responder);
        final TrucoResult trucoResult = truco.handle(hand.getScore());
        HandResult handResult = null;

        if(trucoResult.hasWinner()) {
            winner = trucoResult.getWinner().orElseThrow();
            handResult = new HandResult(trucoResult);
            hand.setResult(handResult);
        }

        trucoResult.getLastRequester().ifPresent(hand::setLastBetRaiser);
        hand.setScore(trucoResult.getScore());

        return Optional.ofNullable(handResult);
    }

    private void validateCards() {
        if(!firstCard.equals(Card.closed()) && firstCard.equals(lastCard))
            throw new GameRuleViolationException("Cards in the deck must be unique!");
        if(!firstCard.equals(Card.closed()) && firstCard.equals(vira))
            throw new GameRuleViolationException("Cards in the deck must be unique!");
        if(!lastCard.equals(Card.closed()) && lastCard.equals(vira))
            throw new GameRuleViolationException("Cards in the deck must be unique!");
    }

    public Optional<Card> getHighestCard() {
        if (firstCard.compareValueTo(lastCard, vira) == 0) return Optional.empty();
        return firstCard.compareValueTo(lastCard, vira) > 0 ? Optional.of(firstCard) : Optional.of(lastCard);
    }

    public Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    @Override
    public String toString() {
        return "Round{" +
                "winner=" + winner +
                '}';
    }
}