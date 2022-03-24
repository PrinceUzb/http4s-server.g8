# http4s-serve giter8 template

-------------
![CI Status](https://github.com/PrinceUzb/http4s-server.g8/workflows/Build/badge.svg)
[![MergifyStatus](https://img.shields.io/endpoint.svg?url=https://gh.mergify.io/badges/PrinceUzb/http4s-server.g8&style=flat)](https://mergify.io)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-brightgreen.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
Generate a http4s server on the blaze backend with Circe.

## Create your project on the command line:

--------------
 ```bash 
g8 PrinceUzb/http4s-server.g8
```

or to use directly from sbt you can use:
 ```sbt 
sbt new PrinceUzb/http4s-server.g8
```
## Instructions

---------------
1. Create the project as above
2. Run project via command line. On project folder `bash run.sh`
3. Open browser [localhost:9000](http://localhost:9000/)

## Technologies

<table>
  <thead style="background: green">
    <tr style="color: white">
      <th>#</th>
      <th>Technology</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>1</th>
      <td>Http4s</td>
      <td>Http4s is a minimal, idiomatic Scala interface for HTTP services. Http4s is Scala's answer to Ruby's Rack, Python's WSGI, Haskell's WAI, and Java's Servlets.</td>
    </tr>
    <tr>
      <th>2</th>
      <td>Circe</td>
      <td>Circe is a JSON library for Scala (and Scala.js) powered by Cats.</td>
    </tr>
    <tr>
      <th>3</th>
      <td>Cats</td>
      <td>Cats is a library which provides abstractions for functional programming in the Scala programming language. The name is a playful shortening of the word category.</td>
    </tr>
    <tr>
      <th>4</th>
      <td>Cats Effect</td>
      <td>Cats Effect is a high-performance, asynchronous, composable framework for building real-world applications in a purely functional style within the <a href="https://typelevel.org/">Typelevel</a> ecosystem.</td>
    </tr>
    <tr>
      <th>5</th>
      <td>Skunk</td>
      <td>Skunk is a data access library for Scala + Postgres powered by cats, cats-effect, scodec, and fs2.</td>
    </tr>
    <tr>
      <th>6</th>
      <td>Ciris</td>
      <td>Ciris is a functional programming library for loading configurations.</td>
    </tr>
    <tr>
      <th>7</th>
      <td>Refined</td>
      <td>Refined is a Scala library for refining types with type-level predicates which constrain the set of values described by the refined type. </td>
    </tr>
  </tbody>
</table>