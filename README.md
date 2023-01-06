
<!-- PROJECT LOGO -->
<br/>
<div align="center">
  <a href="https://github.com/C1rF/VisualNeo">
    <img src="readmeImages/visualneo_icon.png" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">VisualNeo</h3>

  <p align="center">
    VisualNeo is a visual query interface that is compatible with Neo4j. It helps non-expert users to build graphical queries without learning any Cypher language.
  </p>
</div>
<br/>


<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

![Product Name Screen Shot][product-screenshot]

This project is a 2022-2023 COMP4981 Final Year Project of Hong Kong University of Science and Technology. It is implemented by Group XZ2. 

The intention of the project is to respond to the increasing needs to query a graph database. Among all people using databases, many do not have computer science background and thus may face difficulties.

Fortunately, graph queries are close to human intuition and can be represented by shapes like vertices and edges. Consequently, Visual Query Interfaces (VQIs), an application on the top layer that enables users to draw graph queries intuitively, have become a feasible solution.

VisualNeo includes many cutting-edge functions such as data-driven VQI design, action-aware graph query processing, and effective query results visualization.  

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

* [![Next][IntelliJ]][IntelliJ-url]
* [![React][Maven]][Maven-url]
* [![Vue][Neo4j]][Neo4j-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->
## Getting Started

Please follow the instructions below to set up your VisualNeo locally:

### Prerequisites


* [Neo4j Desktop](https://neo4j.com/download-center/) or [Neo4j Sandbox](https://sandbox.neo4j.com/)
* JDK 17+
* Apache Maven 3.3+
* JavaFX 19

  

### Installation & Running

1. Clone the repo
   ```sh
   git clone https://github.com/C1rF/VisualNeo
   ```
2. Open the project using IntelliJ or equivalent IDEs and run the project
3. Or run the following command in the root directory:
   ```sh
   mvn clean javafx:run
   ```
4. Or run the executable jar file using the following command in the root directory:
   ```sh
   mvn clean package
   ```
   ```
   java --module-path [your own javafx lib path] --add-modules javafx.controls,javafx.fxml -jar target/VisualNeo-1.0.0.jar
   ```
   JavaFX is separated from JDK 11+ and needed to installed independently by users. To download JavaFX, click [here](https://gluonhq.com/products/javafx/). Use the path of lib directory as your command line input.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Usage

Here is a quick guideline of how VisualNeo can be used.

1. Load the remote or local database by providing security data (uri, username, password)
   ![Load Database Screen Shot][load-database-screenshot]
2. View the metadata of the loaded database
   ![Database Metadata Screen Shot][database-metadata-screenshot]
3. Construct graph queries by pressing:

   **Shift**: Create nodes/edges

   **Ctrl**: Select Multiple nodes/edges

   **Backspace/Delete**: Delete nodes/edges
4. Save/Load self-defined patterns (if needed)
5. Load the recommended patterns from file (if needed)
   ![Load Recommended Pattern Screen Shot][load-pattern-screenshot]
6. Fire the search and view the returned results
   ![Display Result Screen Shot][display-result-screenshot]

_For more examples, please refer to the manual._

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTACT -->
## Contact

LIANG Houdong - [@website](https://dongdong3272.github.io/) - hliangam@connect.ust.hk

YAO Chongchong - cyaoad@connect.ust.hk

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[product-screenshot]: readmeImages/visualneo_ui.png
[load-database-screenshot]: readmeImages/load-database.png
[database-metadata-screenshot]: readmeImages/database-metadata.png
[load-pattern-screenshot]: readmeImages/load-pattern.png
[display-result-screenshot]: readmeImages/display-results.png
[IntelliJ]: https://img.shields.io/badge/IntelliJ-000000?style=for-the-badge&logo=intellijidea&logoColor=white
[IntelliJ-url]: https://www.jetbrains.com/idea/
[Maven]: https://img.shields.io/badge/maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white
[Maven-url]: https://maven.apache.org/
[Neo4j]: https://img.shields.io/badge/neo4j-4581C3?style=for-the-badge&logo=neo4j&logoColor=white
[Neo4j-url]: https://neo4j.com/


