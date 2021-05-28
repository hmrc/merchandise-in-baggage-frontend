
# merchandise-in-baggage-frontend

**Who uses the repo/service**

Business travellers carrying commercial goods for both import or export.

**How to start the service locally**

`sbt run` This will only start the service as standalone but unable to interact with any other services including Backend and DataBase

SM profile : MERCHANDISE_IN_BAGGAGE_ALL
`sm MERCHANDISE_IN_BAGGAGE_ALL` This will start all the required services to complete a journey

`local url` http://localhost:8281/declare-commercial-goods/start-import
`local url` http://localhost:8281/declare-commercial-goods/start-export

**How to run tests**

`sbt test` will run all the tests, including unit, UI and consumer contract tests. The consumer tests will generate
contract files stored in the project root directory folder `pact`
The generated contracts test will then being used from the Backend contract verifier by running the script:
`checkincheck.sh`. However, currently contracts test only runs for local build.

#Endpoints:
GET         /language/:lang              
GET        /assets/*file                 

GET        /keepalive                    
GET        /progress-deleted             
GET        /service-timed-out            

**Invalid journey state**

GET        /cannot-access-page           

**Start Import**

GET        /start-import                 
GET        /process-start-import         

**Start Export**

GET        /start-export                 
GET        /process-start-export         

**New Or Existing**

GET        /new-or-existing              
POST       /new-or-existing              

**Excise And Restricted Goods**

GET        /excise-and-restricted-goods  
POST       /excise-and-restricted-goods  

**Goods Destination**

GET        /goods-destination            
POST       /goods-destination            

**Value Weight Of Goods**

GET        /value-weight-of-goods        
POST       /value-weight-of-goods        

**Search Goods**

GET        /goods-type/:idx              
POST       /goods-type/:idx              

**Goods Vat Rate**

GET        /goods-vat-rate/:idx          
POST       /goods-vat-rate/:idx          

**Goods Origin**

GET        /goods-origin/:idx            
POST       /goods-origin/:idx            

**Search Goods Country**

GET        /search-goods-country/:idx    
POST       /search-goods-country/:idx    

**Purchase Details**

GET        /purchase-details/:idx        
POST       /purchase-details/:idx        

**Review Goods**

GET        /review-goods                 
POST       /review-goods                 

**Remove Goods**

GET        /remove-goods/:idx            
POST       /remove-goods/:idx            

**Goods Removed**

GET        /goods-removed                

**Goods Over Threshold**

GET        /goods-over-threshold         

**Payment Calculation**

GET        /payment-calculation          

**Traveller Details**

GET        /traveller-details            
POST       /traveller-details            

**Enter Email**

GET        /enter-email                  
POST       /enter-email                  

**Customs Agent**

GET        /customs-agent                
POST       /customs-agent                

**Agent Details**

GET        /agent-details                
POST       /agent-details                

**Enter Agent Address**

GET        /enter-agent-address          
GET        /address-lookup-return        

**Eori Number**

GET        /enter-eori-number            
POST       /enter-eori-number            

**Journey Details**

GET        /journey-details              
POST       /journey-details              

**Goods In Vehicle**

GET        /goods-in-vehicle             
POST       /goods-in-vehicle             

**Vehicle Size**

GET        /vehicle-size                 
POST       /vehicle-size                 

**Vehicle Registration Number**

GET        /vehicle-registration-number  
POST       /vehicle-registration-number  

**Cannot Use Service**

GET        /cannot-use-service           

**Cannot Use Service Ireland**
GET        /cannot-use-service-ireland   

**No Declaration Needed**
GET        /no-declaration-needed        

**Check Your Answer**

GET        /check-your-answers           
POST       /check-your-answers           
GET        /check-your-answers/add-goods 

**Declaration Confirmation**

GET        /declaration-confirmation     
GET        /make-another-declaration     
GET        /add-goods-to-an-existing-declaration     


**Declaration Not Found**
GET        /declaration-not-found        

**Retrieve Declaration**

GET        /retrieve-declaration         
POST       /retrieve-declaration         

**Previous Declaration Details**

GET        /previous-declaration-details        
POST       /previous-declaration-details        

GET         /start-survey                
