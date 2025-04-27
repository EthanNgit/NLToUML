package com.group15.nltouml.generation.uml

import com.group15.nltouml.model.DiagramType
import com.group15.nltouml.util.titlecase
import net.sourceforge.plantuml.SourceStringReader
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service("uml_plantuml")
class PlantUMLEngine: UMLEngine {
    val themeMap: Map<Int, String> = listOf(
        "none", "amiga", "aws-orange", "black-knight", "bluegray", "blueprint", "carbon-gray",
        "cerulean-outline", "cerulean", "cloudscape-design", "crt-amber", "crt-green",
        "cyborg-outline", "cyborg", "hacker", "lightgray", "mars", "materia-outline",
        "materia", "metal", "mimeograph", "minty", "mono", "plain",
        "reddress-darkblue", "reddress-darkgreen", "reddress-darkorange", "reddress-darkred",
        "reddress-lightblue", "reddress-lightgreen", "reddress-lightorange", "reddress-lightred",
        "sandstone", "silver", "sketchy-outline", "sketchy", "spacelab-white", "spacelab",
        "Sunlust", "superhero-outline", "superhero", "toy", "united", "vibrant"
    ).withIndex().associate { it.index to it.value }

    override fun textToDiagram(input: String, themeId: Int): ByteArray {
        val inputString = addThemeToInput(input, themeId)

        val reader = SourceStringReader(inputString)
        val outputStream = ByteArrayOutputStream()
        val desc = reader.outputImage(outputStream)

        if (desc == null || desc.description?.contains("(Error)") == true) {
            throw IllegalArgumentException("PlantUML syntax error detected.")
        }

        return outputStream.toByteArray()
    }

    private fun addThemeToInput(input: String, themeId: Int): String {
        if (!input.contains("@enduml") || !input.contains("@startuml")) {
            throw IllegalArgumentException("PlantUML syntax error detected.")
        }
        val theme = themeMap[themeId] ?: return input

        // add "!theme name" after @startuml
        return input.replaceFirst("@startuml", "@startuml\n!theme $theme")
    }

    override fun getSyntaxForDiagramType(type: DiagramType): String {
        return when (type) {
            DiagramType.Class -> """
            PlantUML, here is a rundown of some basic syntax for reference, do not use color, theme, note, or comments.

            @startuml
            ' Diagram Type: Class
            ' Example System: Library System
            
            abstract class LibraryItem {
              -itemId: String
              +getTitle(): String
              +getPublicationYear(): int
            }
            
            interface Searchable {
              +search(query: String): List  ' returns List<LibraryItem>
            }
            
            enum ItemStatus {
              AVAILABLE
              LOANED
              DAMAGED
            }
            
            class Book {
              -isbn: String
              -author: String
              +getNumberOfPages(): int
            }
            
            class DVD {
              -durationMinutes: int
              +getAspectRatio(): String
            }
            
            class Patron {
              -patronId: int
              +name: String
              {static} MAX_LOANS: int
              +borrowItem(item: LibraryItem): boolean
            }
            
            LibraryItem <|-- Book
            LibraryItem <|-- DVD
            Book ..|> Searchable
            DVD ..|> Searchable
            
            Patron "1" -- "0..*" Loan : places
            
            class Loan {
              -loanDate: Date
              -dueDate: Date
              +isOverdue(): boolean
            }
            
            Loan *-- "1" LibraryItem : covers
            Loan --> Patron
            
            package "Library Management" {
              class Catalog {
                +findItem(id: String): LibraryItem
              }
              Catalog --> LibraryItem : manages
            }
            @enduml
        """.trimIndent()
            DiagramType.UseCase -> """
            PlantUML, here is a rundown of some basic syntax for reference, do not use color, theme, note, or comments.

            @startuml
            ' Diagram Type: Use Case
            ' Example System: Ride Share System
    
            :Rider: as R
            actor Driver as D
            actor "Payment Gateway" as PG
    
            rectangle "Ride Share Platform" {
              (Request Ride) as ReqRide
              usecase "Accept Ride" as AcceptRide
              usecase "Track Ride" as TrackRide
              usecase "Process Payment" as ProcPayment
              usecase "Rate Driver" as RateDriver
            }
    
            R -- ReqRide
            D -- AcceptRide
            R -- TrackRide
            D -- TrackRide
            R -- ProcPayment
            R -- RateDriver
    
            ReqRide .> ProcPayment : <<include>>
            ProcPayment -- PG
    
            TrackRide .> RateDriver : <<extend>>
    
            @enduml
        """.trimIndent()
            DiagramType.Component -> """
            PlantUML, here is a rundown of some basic syntax for reference, do not use color, theme, note, or comments.

            @startuml
            ' Diagram Type: Component
            ' Example System: Online Flashcard System
            
            package "Client Side" {
              component [Frontend Web App] as Frontend
            }
            
            node "Server Infrastructure" {
              package "Application Services" {
                component [Backend API Service] as Backend
                component [Authentication Service] as Auth
              }
              database [Database] as DB
            }
            
            interface "Flashcard API" as FlashcardAPI
            interface "Authentication API" as AuthAPI
            
            Frontend --> FlashcardAPI : uses
            Frontend --> AuthAPI : uses
            
            FlashcardAPI <|.. Backend
            AuthAPI <|.. Auth
            
            Backend --> DB : reads/writes
            Backend --> Auth : validates
            @enduml
        """.trimIndent()
            DiagramType.Sequence -> """
            PlantUML, here is a rundown of some basic syntax for reference, do not use color, theme, note, or comments.

            @startuml
            ' Diagram Type: Sequence
            ' Example System: ATM Withdrawal Process
    
            autonumber
    
            actor Customer
            participant ATM
            participant "Bank Server" as Bank
            database AccountDatabase as DB
    
            Customer -> ATM : Insert Card
            activate ATM
    
            ATM -> ATM : Read Card Data
    
            ATM -> Bank : Verify Card(CardData)
            activate Bank
    
            Bank -> DB : Lookup Account(CardData)
            activate DB
            DB --> Bank : AccountDetails
            deactivate DB
    
            Bank --> ATM : Card Validated
            deactivate Bank
    
            ATM -> Customer : Request PIN
            Customer -> ATM : Enter PIN
    
            ATM -> Bank : Verify PIN(CardData, PIN)
            activate Bank
            Bank --> ATM : PIN Validated
            deactivate Bank
    
            Customer -> ATM : Request Withdrawal(Amount)
    
            ATM -> Bank : Process Withdrawal(CardData, Amount)
            activate Bank
    
            Bank -> DB : Check Balance(CardData)
            activate DB
            DB --> Bank : CurrentBalance
            deactivate DB
    
            alt Sufficient Funds
              Bank -> DB : Debit Account(CardData, Amount)
              activate DB
              DB --> Bank : Account Updated
              deactivate DB
              Bank --> ATM : Withdrawal Approved
            else Insufficient Funds
              Bank --> ATM : Withdrawal Denied
            end
    
            deactivate Bank
    
            ATM -> Customer : Dispense Cash / Show Error
            ATM -> Customer : Eject Card
    
            deactivate ATM
    
            @enduml
        """.trimIndent()
            DiagramType.Activity -> """
            PlantUML, here is a rundown of some basic syntax for reference, do not use color, theme, note, or comments.

            @startuml
            ' Diagram Type: Activity
            ' Example System: Simple Store Checkout Process
            
            start
            :Customer adds item to cart;
            
            partition "Customer Actions" {
              :View Cart;
              repeat
                :Review items;
                if (Needs removal?) then (yes)
                  :Remove item;
                endif
              repeat while (More items to review?)
              :Proceed to Checkout;
              :Enter Shipping Information;
              :Enter Payment Details;
            }
            
            partition "System Processing" {
              split
                :Validate Shipping Info;
              split again
                :Validate Payment Details;
              end split
            
              if (Info Valid?) then (yes)
                :Initiate Payment Processing;
                if (Payment Successful?) then (yes)
                  :Generate Order Confirmation;
                else (no)
                  :Request correct payment info;
                endif
              else (no)
                :Request correct shipping info;
              endif
            }
            
            partition "Order Fulfillment" {
              :Notify Warehouse;
              split
                :Pick Items;
              split again
                :Pack Items;
              end split
              :Ship Order;
            }
            
            :Notify Customer of Shipment;
            
            stop
            @enduml
        """.trimIndent()
            else -> ""
        }
    }

    override fun getAvailableDiagramTypes(): List<String> {
        // for now hardcode
        return listOf("Class", "Use case", "Component", "Sequence", "Activity")
    }

    override fun getAvailableDiagramThemes(): Map<Int, String> {
        // for now hardcode
        return themeMap.mapValues { formatThemeName(it.value) }
    }

    private fun formatThemeName(name: String): String {
        return titlecase(name.replace("-", " "))
    }

    override fun getAvailableDiagramFileTypes(): List<String> {
        // not used yet, but easily addable, also hardcode for now
        return listOf("png", "svg")
    }
}