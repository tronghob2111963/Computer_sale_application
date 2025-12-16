# AI BUILD PC SUGGESTION - ALGORITHM DESCRIPTION

To help users build a PC configuration that matches their needs and budget, the Computer Sell system provides an AI-powered suggestion feature. This algorithm describes the steps to get AI recommendations for PC components.

## Algorithm Steps

- User enters the requirements to get AI suggestions: use case (Gaming/Creator/Office), target resolution (1080p/1440p/4K), budget in VND, form factor preference (ATX/mATX/ITX), and quiet/cool preference.

- The system will validate all input fields to ensure required information is provided. If the budget is not specified or invalid, the system will use a default budget of 15,000,000 VND.

- The system will collect all available products from the database for each component type: CPU, Mainboard, RAM, GPU, Storage, PSU, Case, and Cooler. Products with stock greater than 0 will be prioritized.

- The system will build an AI prompt containing:
  - System instructions defining the AI as a PC building expert with budget allocation rules
  - User's requirements (use case, resolution, budget, preferences)
  - List of available products with their IDs, names, brands, prices, and stock quantities

- The system will call the Groq AI API (LLaMA 3.1 70B model) to analyze the requirements and select the most suitable components from the available products.

- If the AI response is successful, the system will parse the JSON response to extract:
  - Profile name (e.g., "Gaming 1080p", "Creator Workstation")
  - Suggested parts with product IDs and selection reasons
  - Notes and recommendations from AI

- If the AI call fails or returns an invalid response, the system will fall back to a rule-based suggestion algorithm:
  - Determine the profile based on use case and resolution
  - Allocate budget according to predefined ratios (e.g., Gaming: GPU 40%, CPU 18%, RAM 12%)
  - Select products with prices closest to the target allocation for each component type

- The system will validate each suggested product ID exists in the database and retrieve the full product information including name, brand, price, and stock.

- The system will calculate the estimated total price by summing up all suggested component prices.

- Upon successful processing, the system will return a response containing:
  - Profile name identifying the build type
  - User's input budget
  - Estimated total price
  - AI notes and recommendations
  - List of suggested parts with product details and AI reasoning

- The frontend will display the AI suggestions to the user:
  - Show the profile badge (e.g., "🤖 Gaming 1080p")
  - Display the estimated total price
  - Show AI notes/recommendations
  - For each suggested component, display the product with AI's reason for selection

- The user can review the suggestions and choose to:
  - Accept all suggestions and add them to their build
  - Modify individual components by selecting different products
  - Print a quotation for the suggested configuration
  - Add all components to the shopping cart

- If the user accepts the suggestions, the system will save each component to the user's build by calling the add product API for each part.

- A success message will be displayed confirming the AI-suggested configuration has been applied to the user's PC build.
