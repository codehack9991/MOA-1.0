# Required settings: -DenvironmentName="sim_local" -DuserName="kt46743_sim_local"

Feature: Simulator feature

  @sim_local @smoke
  Scenario: Local FIX TCP Client and Simulator
    Given Simulator start
    #When Wait 100 seconds

    When Client send "@clientRequestNew" RequestNew [OrdType=LIMIT|OrderQty=1000|Side=BUY|Symbol=$Symbol|Price=100]

    Then Simulator receive "@serverRequestNew" RequestNew []
    And it contains [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side]

    When Simulator send "@serverConfirmNew" ConfirmNew [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side|37=%generateClOrdID()%|11=@serverRequestNew.11]

    Then Client receive "@clientConfirmNew" ConfirmNew [11=@clientRequestNew.11]
    And it contains [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side]

    When Client send "@clientRequestCancel" RequestCancel [37=@clientConfirmNew.37|OrigClOrdID=@clientRequestNew.ClOrdID|Symbol=@clientRequestNew.Symbol|Side=@clientRequestNew.Side]

    Then Simulator receive "@serverRequestCancel" RequestCancel [37=@serverConfirmNew.37]
    And it contains [Symbol=@clientRequestNew.Symbol|Side=@clientRequestNew.Side]

    When Simulator send "@serverConfirmCancel" ConfirmCancel [37=@serverConfirmNew.37|11=@serverRequestCancel.11|OrigClOrdID=@serverRequestCancel.OrigClOrdID|Symbol=@serverRequestCancel.Symbol]

    Then Client receive "@clientConfirmCancel" ConfirmCancel [ClOrdID=@clientRequestCancel.ClOrdID]
    And it contains [OrigClOrdID=@clientRequestNew.ClOrdID]



  @sim_local @smoke
  Scenario Outline: Local FIX TCP Client and Simulator with examples
    Given Simulator start
#When Wait 100 seconds

    When Client send "@clientRequestNew" RequestNew [Symbol=$Symbol|OrdType=<OrdType>|Price=100|OrderQty=1000|Side=<Side>]

    Then Simulator receive "@serverRequestNew" RequestNew []
    And it contains [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side]

    When Simulator send "@serverConfirmNew" ConfirmNew [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side|37=%generateClOrdID()%|11=@serverRequestNew.11]

    Then Client receive "@clientConfirmNew" ConfirmNew [11=@clientRequestNew.11]
    And it contains [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side]

    When Client send "@clientRequestCancel" RequestCancel [37=@clientConfirmNew.37|OrigClOrdID=@clientRequestNew.ClOrdID|Symbol=@clientRequestNew.Symbol|Side=@clientRequestNew.Side]

    Then Simulator receive "@serverRequestCancel" RequestCancel [37=@serverConfirmNew.37]
    And it contains [Symbol=@clientRequestNew.Symbol|Side=@clientRequestNew.Side]

    When Simulator send "@serverConfirmCancel" ConfirmCancel [37=@serverConfirmNew.37|11=@serverRequestCancel.11|OrigClOrdID=@serverRequestCancel.OrigClOrdID|Symbol=@serverRequestCancel.Symbol]

    Then Client receive "@clientConfirmCancel" ConfirmCancel [ClOrdID=@clientRequestCancel.ClOrdID]
    And it contains [OrigClOrdID=@clientRequestNew.ClOrdID]

    Examples:
      |OrdType|Side|
      |LIMIT  |BUY |
      |LIMIT  |SELL|

