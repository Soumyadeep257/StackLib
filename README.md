# StackLib 📚

StackLib is a premium, high-performance Library Management System built entirely in Core Java and SQLite. Developed as part of a Java Development Internship at **Pinnacle Labs**, this project transforms standard legacy desktop layouts into a modern, dark-mode SaaS dashboard.

By stepping away from traditional Java Swing UI constraints, StackLib leverages JavaFX CSS styling and relational database architecture to deliver a seamless, state-driven user experience.

## 🚀 Key Features

* **SaaS-Inspired Dark Theme UI:** Features a custom CSS-styled BorderPane architecture with a persistent navigation sidebar, floating data cards, and deep contrast styling (`#1e1e2f` base).
* **Relational SQLite Engine:** Operates on a serverless SQLite backend utilizing a multi-table relational schema (Books, Members, Issues) with strict foreign key constraints.
* **Smart Transaction Logic:** The Issue/Return engine automatically validates stock levels, checks member registration status, dynamically updates inventory quantities, and prevents duplicate or invalid borrowing states.
* **Real-Time Dashboard Analytics:** Executes optimized aggregation queries to display live statistics for total assets, registered members, and active book issues.
* **Complete CRUD Operations:** Fully functional Create, Read, Update, and Delete capabilities for both library assets and student directories, directly mapped to Java `Record` and POJO models.

## 🛠️ Tech Stack

* **Language:** Java SE 17
* **Build Architecture:** Apache Maven
* **UI Framework:** JavaFX (Styled with CSS-in-Java)
* **Database:** SQLite (JDBC Driver)
* **Data Mapping:** Java POJOs & SQL Prepared Statements

## ⚙️ Installation & Setup

To run this application locally, you will need Java 17+ and Maven installed on your machine.

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/Soumyadeep257/StackLib.git](https://github.com/Soumyadeep257/StackLib.git)
   cd StackLib