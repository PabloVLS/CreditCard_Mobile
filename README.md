# Credit Card Mobile UI (Android - Kotlin)

This project is an Android application developed using **Kotlin** and **ConstraintLayout** to simulate a credit card interface.

## Features

- Real-time credit card preview using CardView
- Input form with:
  - Card number
  - Card holder name
  - Expiration date (MM/YY)
  - CVV
- Automatic input formatting:
  - Card number formatted with spaces every 4 digits
  - Expiration date formatted as MM/YY
- Input validation:
  - Card number must contain 16 digits
  - Card holder name must contain at least 3 characters

## Technical Challenges

### Dynamic UI (CardFlipper)
- The card flips to show the back when the CVV field is selected
- The card returns to the front when other fields are selected

### Card Brand Detection
- Identifies the card brand (Visa, Mastercard, etc.) based on the first digits
- Updates the card logo and color dynamically

## Technical Implementation

- Language: Kotlin
- Layout: ConstraintLayout
- Components: CardView, TextWatcher
- Real-time UI updates based on user input

## Repository

https://github.com/PabloVLS/CreditCard_Mobile

## Demo Video

[Add your YouTube unlisted video link here](https://youtu.be/q8YvL7cXSfc)
