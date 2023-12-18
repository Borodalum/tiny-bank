# Tiny bank
This is pure functional bank written in Scala 2.13.2 using Typelevel (cats stack). 

## Requirements
- Scala 2.13.2
- Sbt
- Docker

## Build
To build the Tiny bank, follow these steps:

1. Clone the repository to your local machine or download the source code as a ZIP file.
2. Navigate to the project directory.
3. Compile project using: 
    ```bash
    sbt compile
    ```
   This command compiles the source code.
4. Create docker image:
    ```bash
   sbt docker:publishLocal
   ```
5. Run docker-compose
    ```bash
   docker-compose up
   ```
## Swagger UI
In order to see all Tiny bank features and functions you need to proceed to:
```
http://<host>:<port>/docs
```
## How to use
1. Create Tiny bank client using ```/clients/register``` endpoint. 
2. In order to run any operations with client, firstly you need to login. Do it by using ```/clients/login``` login. After calling that endpoint, you will receive session-token in cookie. 
3. Now, after you logged in into the system, you can create your first bank account. Do it by using ```/accounts/create``` endpoint. You will receive a bank account id. Number of bank accounts not more than three. 
4. You can do following operations with your bank account, using very same id you received early:
   1. Top up: ```/accounts/deposit/{your_id}```
   2. Withdraw: ```/accounts/withdraw/{your_id}```
   3. Current Balance: ```/accounts/balance/{your_id}```
   4. Transaction to other account: ```/accounts/transfer/{your_id}```

## Additional Notes
- This project was developed using Scala 2.13.2 and tapir. Ensure you have Scala 2.13 installed and properly configured on your system.

## Contributing

Contributions to this project are welcome! If you'd like to contribute, please follow these steps:
1. Fork the repository on GitHub.
2. Clone the forked repository to your local machine.
3. Create a new branch for your contribution:
    ```bash
    git checkout -b feature/your-feature
    ```
4. Make your changes and commit them with descriptive commit messages.
5. Push your changes to your forked repository.
6. Create a pull request on the original repository, explaining your changes.

## License
This project is licensed under the MIT License. See the [LICENSE](https://choosealicense.com/licenses/mit/) file for more information.  
