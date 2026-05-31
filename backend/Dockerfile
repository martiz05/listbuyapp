FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app
EXPOSE 8080

FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY ["Directory.Build.props", "./"]
COPY ["backend/src/BuyApp.Domain/BuyApp.Domain.csproj", "backend/src/BuyApp.Domain/"]
COPY ["backend/src/BuyApp.Application/BuyApp.Application.csproj", "backend/src/BuyApp.Application/"]
COPY ["backend/src/BuyApp.Infrastructure/BuyApp.Infrastructure.csproj", "backend/src/BuyApp.Infrastructure/"]
COPY ["backend/src/BuyApp.Api/BuyApp.Api.csproj", "backend/src/BuyApp.Api/"]
RUN dotnet restore "backend/src/BuyApp.Api/BuyApp.Api.csproj"
COPY ["backend/", "backend/"]
RUN dotnet publish "backend/src/BuyApp.Api/BuyApp.Api.csproj" -c Release -o /app/publish --no-restore

FROM runtime AS final
WORKDIR /app
COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "BuyApp.Api.dll"]
