package qa.demo.tests;

import com.google.gson.Gson;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.demo.api.BookApi;
import qa.demo.models.authorization.LoginResponseModel;
import qa.demo.models.books.AddingBookToListModel;
import qa.demo.models.books.GetBookModel;
import qa.demo.models.books.IsbnModel;
import qa.demo.models.books.RemovingBookFromListModel;

import java.util.ArrayList;
import java.util.List;

import static io.qameta.allure.Allure.step;
import static io.qameta.allure.SeverityLevel.NORMAL;
import static org.assertj.core.api.Assertions.assertThat;
import static qa.demo.utils.TestDataVariables.*;
import static qa.demo.utils.TestDataVariables.bookId;

@Epic(value = "Проверка апи сайта https://demoqa.com")
@Feature(value = "Проверка методов, связанных с магазином книг")
@Link(name = "demoqa", value = "https://demoqa.com/")
@Owner("Overloque")
@Tag("bookstore")
public class BookStoreTest extends BaseTest {
    BookApi bookApi = new BookApi();

    @Test
    @Severity(NORMAL)
    @DisplayName("Проверка удаления книги из списка")
    void checkSuccessRemovingBookTest() {
        step("Вызов метода для генерации токена пользователя", () ->
                authorizationApi.generateToken(existCreds));

        step("Вызов метода авторизации", () -> {
            LoginResponseModel response = authorizationApi.login(existCreds);
            System.setProperty("response", new Gson().toJson(response));
        });

        step("Подготовка данных для действий по удалению/добавлению книги", () -> {
            String responseJson = System.getProperty("response");
            LoginResponseModel response = new Gson().fromJson(responseJson, LoginResponseModel.class);

            IsbnModel isbnModel = new IsbnModel(bookId);

            List<IsbnModel> collectionIsbn = new ArrayList<>();
            collectionIsbn.add(isbnModel);

            AddingBookToListModel booksList = new AddingBookToListModel(response.getUserId(), collectionIsbn);

            RemovingBookFromListModel removingBooksList = new RemovingBookFromListModel(bookId, response.getUserId());

            System.setProperty("booksList", new Gson().toJson(booksList));
            System.setProperty("removingBooksList", new Gson().toJson(removingBooksList));
        });

        step("Вызов метода для удаления всех книг", () -> {
            String responseJson = System.getProperty("response");
            LoginResponseModel response = new Gson().fromJson(responseJson, LoginResponseModel.class);

            bookApi.removeAllBooks(response);
        });

        step("Вызов метода для удаления всех книг", () -> {
            String responseJson = System.getProperty("response");
            String booksListJson = System.getProperty("booksList");

            LoginResponseModel response = new Gson().fromJson(responseJson, LoginResponseModel.class);
            AddingBookToListModel booksList = new Gson().fromJson(booksListJson, AddingBookToListModel.class);

            bookApi.addBook(response, booksList);
        });

        step("Вызов метода удаления одной книги", () -> {
            String responseJson = System.getProperty("response");
            String removingBooksListJson = System.getProperty("removingBooksList");

            LoginResponseModel response = new Gson().fromJson(responseJson, LoginResponseModel.class);
            RemovingBookFromListModel removingBooksList = new Gson().fromJson(removingBooksListJson, RemovingBookFromListModel.class);

            bookApi.removeBook(response, removingBooksList);
        });
    }

    @Test
    @Severity(NORMAL)
    @DisplayName("Проверка полей на странице книги")
    void checkBookFieldsTest() {
        step("Вызов метода для генерации токена пользователя", () ->
                authorizationApi.generateToken(existCreds));

        step("Вызов метода авторизации", () ->
                authorizationApi.login(existCreds));

        step("Вызов метода получения книги", () -> {
            GetBookModel response = bookApi.getBook(bookId);
            System.setProperty("response", new Gson().toJson(response));
        });

        step("Проверка полей у книги", () -> {
            String responseJson = System.getProperty("response");
            GetBookModel response = new Gson().fromJson(responseJson, GetBookModel.class);

            assertThat(response.getAuthor())
                    .as("Автор книги")
                    .isEqualTo("Richard E. Silverman");

            assertThat(response.getPublishDate())
                    .as("Дата публикации книги")
                    .isEqualTo("2020-06-04T08:48:39.000Z");

            assertThat(response.getPublisher())
                    .as("Издатель")
                    .isEqualTo("O'Reilly Media");

            assertThat(response.getTitle())
                    .as("Название книги")
                    .isEqualTo("Git Pocket Guide");

            assertThat(response.getWebsite())
                    .as("Вебсайт")
                    .isEqualTo("http://chimera.labs.oreilly.com/books/1230000000561/index.html");
        });
    }
}