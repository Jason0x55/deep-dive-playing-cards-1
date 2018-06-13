package edu.cnm.deepdive.cards;

import edu.cnm.deepdive.cards.Deck.InsufficientCardsException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * This class implements a console-mode, one-player (plus computer dealer)
 * Blackjack game. Only basic game play and betting actions are supported; in
 * particular, doubling down and splitting are not supported at all, and only a
 * limited form of insurance is supported.
 *
 * @author Nicholas Bennett &amp; Deep Dive Coding Java Cohort 4
 */
public class ConsoleGame {

  private static final int INITIAL_POT = 100;
  private static final int MAX_BET = 10;
  private static final Pattern NON_WHITE_SPACE = Pattern.compile("\\S+");
  private static final String RESOURCE_BUNDLE = "resources/console_game";
  private static final String POT_AMOUNT_KEY = "pot_amount_pattern";
  private static final String DEALERS_HAND_PATTERN_KEY = "dealers_hand_pattern";
  private static final String LEAVE_THE_TABLE_PATTERN_KEY = "leave_the_table_pattern";
  private static final String YOUR_BET_PATTERN_KEY = "your_bet_pattern";
  private static final String INSURANCE_PROMPT_KEY = "insurance_prompt";
  private static final String YES_INPUT_CHAR_KEY = "yes_input_char";
  private static final String NO_INPUT_CHAR_KEY = "no_input_char";
  private static final String YOUR_PLAY_PROMPT_KEY = "your_play_prompt";
  private static final String DEALERS_PLAY_MSG_KEY = "dealers_play_msg";
  private static final String FINAL_DEALERS_HAND_PATTERN_KEY = "final_dealers_hand_pattern";
  private static final String YOU_WON_PATTERN_KEY = "you_won_pattern";
  private static final String YOU_LOST_PATTERN_KEY = "you_lost_pattern";
  private static final String PUSH_KEY = "push";

  private static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);

  /**
   *
   * @param args
   */
  public static void main(String... args) {
    try (Scanner scanner = new Scanner(System.in)) {
      Random rng = new SecureRandom();
      Deck deck = new Deck();
      int pot = INITIAL_POT;
      for (boolean play = true; play; play &= pot > 0) {
        System.out.printf(bundle.getString(POT_AMOUNT_KEY), pot);
        int bet = getBet(scanner, pot);
        if (bet > 0) {
          deck.gather();
          deck.shuffle(rng);
          BlackjackHand dealer = new BlackjackDealerHand(deck);
          BlackjackHand player = new InteractiveBlackjackHand(deck, scanner);
          Card topCard = dealer.getHand()[1];
          System.out.printf(bundle.getString(DEALERS_HAND_PATTERN_KEY), topCard);
          if (!player.isBlackjack()
              || (topCard.getRank() != Rank.ACE)
              || !buyInsurance(scanner, player)) {
            pot += playHands(scanner, player, dealer, bet);
          }
        } else {
          play = false;
        }
      }
      System.out.printf(bundle.getString(LEAVE_THE_TABLE_PATTERN_KEY), pot);
    } catch (InsufficientCardsException e) {
      /*
      In this program, this exception should never occur. If it does, wrap it in
      and throw a RuntimeException, terminating the program.
      */
      throw new RuntimeException(e);
    }
  }

  private static int getBet(Scanner scanner, int pot) {
    int bet = -1;
    int maxBet = Math.min(10, MAX_BET);
    do {
      System.out.printf(bundle.getString(YOUR_BET_PATTERN_KEY), maxBet);
      while (!scanner.hasNext()) {}
      if (scanner.hasNextInt()) {
        int input = scanner.nextInt();
        if (input >= 0 && input <= maxBet) {
          bet = input;
        }
      }
      scanner.nextLine();
    } while (bet < 0);
    return bet;
  }

  private static boolean buyInsurance(Scanner scanner, BlackjackHand player) {
    Boolean insure = null;
    System.out.println(player);
    while (insure == null) {
      System.out.print(bundle.getString(INSURANCE_PROMPT_KEY));
      while (!scanner.hasNext(NON_WHITE_SPACE)) {}
      char input = scanner.next(NON_WHITE_SPACE).toLowerCase().charAt(0);
      if (input == bundle.getString(YES_INPUT_CHAR_KEY).charAt(0)) {
        insure = true;
      } else if (input == bundle.getString(NO_INPUT_CHAR_KEY).charAt(0)) {
        insure = false;
      }
      scanner.nextLine();
    }
    return insure;
  }

  private static int playHands(Scanner scanner, BlackjackHand player, BlackjackHand dealer, int bet)
      throws InsufficientCardsException {
    int gain = 0;
    System.out.printf(bundle.getString(YOUR_PLAY_PROMPT_KEY));
    player.play();
    System.out.printf(bundle.getString(DEALERS_PLAY_MSG_KEY));
    if (!player.isBusted()) {
      dealer.play();
    }
    System.out.printf(bundle.getString(FINAL_DEALERS_HAND_PATTERN_KEY), dealer);
    int comparison = player.compareTo(dealer);
    if (comparison > 0) {
      gain = player.isBlackjack() ? bet * 3 / 2 : bet;
      System.out.printf(bundle.getString(YOU_WON_PATTERN_KEY), gain);
    } else if (comparison < 0 || dealer.isBlackjack()) {
      gain = -bet;
      System.out.printf(bundle.getString(YOU_LOST_PATTERN_KEY), bet);
    } else {
      System.out.printf(bundle.getString(PUSH_KEY));
    }
    return gain;
  }

}
