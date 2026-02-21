# 💳 Credit Card Mobile UI (Android - Kotlin)

An Android application built with **Kotlin** and **ConstraintLayout** that simulates a real credit card interface with dynamic preview, input validation, and brand detection.

---

## 🚀 Features

- 📱 Real-time credit card preview using **CardView**
- 🧾 Interactive input form:
  - Card Number
  - Card Holder Name
  - Expiration Date (MM/YY)
  - CVV
- ✨ Automatic input formatting:
  - Card number formatted with spaces every 4 digits
  - Expiration date formatted as MM/YY
- ✅ Input validation:
  - Card number must contain exactly 16 digits
  - Card holder name must contain at least 3 characters

---

## 🎯 Technical Challenges

### 🔄 Dynamic UI (CardFlipper)
- Automatically flips the card to the back when the CVV field is selected
- Returns to the front when other fields are selected
- Provides a realistic card interaction experience

### 🏷️ Card Brand Detection
- Detects the card brand (Visa, Mastercard, etc.) based on the first digits
- Dynamically updates the card logo and background color

---

## 🛠️ Technical Implementation

- **Language:** Kotlin  
- **Layout:** ConstraintLayout  
- **UI Components:** CardView, CardFlipper  
- **Logic:** TextWatcher for real-time updates  
- **Architecture:** Real-time UI synchronization with user input  

---

## 📂 Repository

🔗 https://github.com/PabloVLS/CreditCard_Mobile

---

## 🎥 Demo Video

📺 https://youtu.be/q8YvL7cXSfc

---

## 📸 Preview
<img width="318" height="575" alt="image" src="https://github.com/user-attachments/assets/89998700-9a8e-4a1a-8535-a85a2086de61" />


<img width="318" height="575" alt="image" src="https://github.com/user-attachments/assets/146df83c-c494-404c-b70e-21b956096666" />

