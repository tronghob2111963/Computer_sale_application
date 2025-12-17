# AI PC Build Suggestion System Workflow Description

## 1. Overview

The AI PC Build Suggestion System is an intelligent feature designed to help users create optimal PC configurations based on their specific requirements and budget. The system leverages the Groq AI API with the LLaMA 3.1 70B Versatile model to analyze user needs and recommend suitable components from the available product inventory. This workflow integrates the Angular frontend, Spring Boot backend, PostgreSQL database, and external AI services to deliver personalized PC build suggestions.

## 2. Workflow Description

The workflow begins when a user accesses the PC Builder page and enters their requirements. Users can specify their use case (Gaming, Creator/Graphics, or Office), target display resolution (1080p, 1440p, or 4K), total budget in VND, preferred form factor (ATX, mATX, or ITX), and whether they prioritize a quiet system. Additionally, users can provide a free-text description detailing specific requirements such as "I want to build a PC for playing AAA games like Cyberpunk 2077 and streaming on Twitch."

Once the user submits their requirements, the frontend component (PcBuilderComponent) sends a POST request to the backend endpoint `/builds/ai-suggest`. The backend service (BuildAiSuggestionService) first sanitizes the budget input and then queries the database to collect available products for all nine component categories: CPU, Mainboard, RAM, GPU, Storage, PSU, Cooler, Case, and Monitor. For each category, the system retrieves the top 10 in-stock products with their prices and specifications.

If products are available, the system proceeds to build the AI prompt. The system prompt contains PC building principles, budget allocation rules for different use cases, and the required JSON response format. The user prompt includes all user requirements, the free-text description (if provided), and the complete list of available products with their IDs, names, prices, and stock quantities. This comprehensive prompt is then sent to the Groq AI API for processing.

## 3. AI Processing and Response Handling

The Groq AI API processes the request using the LLaMA 3.1 70B Versatile model with a maximum of 2048 tokens and a temperature setting of 0.7. The AI analyzes the user's requirements and available products to select the most suitable components for each category. The response includes a profile name (e.g., "Gaming 1080p"), a recommendation note, and a list of suggested parts with product IDs and reasons for selection.

If the AI response is successful, the system parses the JSON response and validates each product ID against the database. If the AI fails or returns an error, the system automatically falls back to a rule-based suggestion mechanism. This fallback uses predefined budget allocation percentages based on the use case. For example, a Gaming 1080p build allocates 15% to CPU, 32% to GPU, 10% to RAM, and distributes the remaining budget across other components proportionally.

## 4. Result Calculation and Display

After obtaining the suggested parts (either from AI or fallback), the system calculates the estimated total price by summing all component prices. The BuildSuggestResponse object is constructed containing the profile name, original budget input, estimated total, AI note, and the list of suggested parts with detailed information including product type, ID, name, brand, price, stock, and selection reason.

The frontend receives this response and displays the results to the user. The interface shows a profile badge indicating the configuration type, the estimated total price, the AI's recommendation note, and all nine suggested components with their respective prices and AI-generated reasons for selection. Users can review the suggestions and either accept the configuration, modify individual components, adjust quantities, or re-run the AI suggestion with different parameters.

## 5. Error Handling and Performance

The system implements robust error handling at multiple levels. If no products are available in the database, an appropriate error message is returned to the user. AI API failures trigger the fallback mechanism to ensure users always receive suggestions. Invalid product IDs are skipped while processing continues with valid products. Rate limiting is handled with exponential backoff, allowing up to three retry attempts before failing.

In terms of performance, the database query typically completes within 200 milliseconds, while the AI API response takes between 2 to 5 seconds depending on the complexity of the request. The total response time ranges from 3 to 6 seconds for AI-powered suggestions, while fallback suggestions complete in approximately 500 milliseconds. This ensures a responsive user experience while delivering intelligent, personalized PC configuration recommendations.
