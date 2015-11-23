rem install JDK with XML support or add libs in the path
rem start javaw
java -cp ".\lib\addressbook.jar;.\ext\lib\aldan3.jar" -DjAddressBook.home=.\data -Daddressbook.DTD=file:./data addressbook.AddressBookFrame