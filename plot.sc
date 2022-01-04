__config()->
{
    'commands' ->
    {
        '' -> _() -> print(player(), ''),
        'claim <name>' -> _(name) -> plot_claim(name, 'lime', false),
        'claim <name> <color>' -> _(name, color) -> plot_claim(name, color, false),
        'claim <name> <color> overwrite' -> _(name, color) -> plot_claim(name, color, true),
        'unclaim' -> _() -> plot_unclaim(false),
        'unclaim confirm' -> _() -> plot_unclaim(true),
        'modify <name> tp <dimension> <location> <rotation>' -> _(name, dimension, location, rotation) -> plot_modify(name, 'tp', [dimension, location:0, location:1, location:2, rotation:1, rotation:2]),
        'modify <name> name <newName>' -> _(name, newName) -> plot_modify(name, 'name', newName),
        'tp <name>' -> 'plot_tp',
        'query' -> _() -> plot_query(player()~'pos'),
        'join <name>' -> 'plot_join',
        'delete <name>' -> 'plot_delete',
        'list' -> 'plot_list'
    },
    'arguments' ->
    {
        'name' -> {'type' -> 'string'},
        'newName' -> {'type' -> 'string'},
        'color' -> {'type' -> 'string', 'options' -> ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black', 'none']}
    },
    'scope' -> 'global'
};

plot_claim(name, color, overwrite) ->
(
    player = player();
    pos = player~'pos' / 512;
    pos = [player~'dimension', ceil(pos:0) - 1, ceil(pos:2) - 1];

    if(list_files('', 'json')~'plots' == null,
        write_file('plots', 'json', {'plots' -> [{'owners' -> [player], 'positions' -> [pos], 'name' -> name, 'tp' -> [pos:0, pos:1 * 512 + 256, 1, pos:2 * 512 + 256, 180, 0], 'subplots' -> []}]});
        print(player, format('r Region ', 't ' + pos, 'r  claimed for new plot ', 't ' + name, 'r  by ', 't ' + player));
        highlight_region(pos, color);
        return();
    );

    plots = read_file('plots', 'json');

    i = 0;
    j = 0;
    for(plots:'plots', plot = _; j += 1; for(plot:'positions',
        if(_ == pos,
            if(plot:'name' == name,
                print(player, format('l This region is already part of ', 't ' + name));
                return()
            );
            if(overwrite,
                if(length(plot:'positions') > 1, delete(plot:'positions', i),
                    print(player, format('l Overwriting this region will delete plot ', 't ' + plot:'name', 'l . If you want to continue, run ', 'm [/plot delete ' + plot:'name' + ']', '!/plot delete ' + plot:'name', 'l  and then you can claim this region'));
                    return()
                );
                if(!add_region_to_plot(plots, pos, name, color, player),
                    new_plot_entry(plots, pos, name, player, color, player)
                )
            ,    
                if(length(plot:'positions') == 1,
                    print(player, format('l Overwriting this region will delete plot ', 't ' + plot:'name', 'l . If you want to continue, run ', 'm [/plot delete ' + plot:'name' + ']', '!/plot delete ' + plot:'name', 'l  and then you can claim this region')),
                print(player, format('l Region already taken by plot ', 't ' + plot:'name', 'l . Run ', 'm [/plot claim ' + name + ' <color> overwrite]', '!/plot claim ' + name + ' ' + color + ' overwrite', 'l  to overwrite it'))
                )
            );
            return()
        );
        i += 1;
    ));

    if(!add_region_to_plot(plots, pos, name, color, player),
        new_plot_entry(plots, pos, name, player, color, player)
    )
);

add_region_to_plot(plots, pos, name, color, player) ->
(
    for(plots:'plots', if(_:'name' == name,
        _:'positions':length(_:'positions') = pos;
        write_file('plots', 'json', plots);
        print(player, format('r Region ', 't ' + pos, 'r  added to plot ', 't ' + name));
        highlight_region(pos, color);
        return(true)
    ));
    return(false)
);

new_plot_entry(plots, pos, name, owners, color, player) ->
(
    plots:'plots':length(plots:'plots') = {'owners' -> owners, 'positions' -> [pos], 'name' -> name, 'tp' -> [pos:0, pos:1 * 512 + 256, 1, pos:2 * 512 + 256, 180, 0], 'subplots' -> []}; 
    print(player, format('r Region ', 't ' + pos, 'r  claimed for new plot ', 't ' + name, 'r  by ', 't ' + owners));
    highlight_region(pos, color);
    write_file('plots', 'json', plots);
);

plot_unclaim(confirm) ->
(
    plots = read_file('plots', 'json');
    player = player();
    pos = player~'pos' / 512;
    pos = [player~'dimension', ceil(pos:0) - 1, ceil(pos:2) - 1];

    i = 0;
    j = 0;
    for(plots:'plots',
        plot = _;
        for(plot:'positions',
            if(_ == pos,
                if(length(plot:'positions') == 1,
                        print(player, format('l Unclaiming this region will delete plot ', 't ' + plot:'name', 'l . If you want to continue, run ', 'm [/plot delete ' + plot:'name' + ']', '!/plot delete ' + plot:'name'));
                        return()
                );
                if(confirm,
                    delete(plot:'positions', j);
                    write_file('plots', 'json', plots);
                    print(player, format('r Unclaiming region ', 't ' + pos, 'r  from ', 't ' + plot:'name'));
                    unhighlight_region(pos);
                    return()
                ,
                    print(player, format('r Current region is part of ', 't ' + plot:'name', 'r , owned by ', 't ' + plot:'owners', 'r , with ', 't ' + length(plot:'positions'),
                    'r  regions total. Click ', 'm HERE', '!/plot unclaim confirm', 'r  to confirm'));
                );
                return()
            );
            j += 1
        );
        i += 1
    );
    print(player, format('l Current region is unclaimed'))
);

plot_query(pos) ->
(
    plots = read_file('plots', 'json');
    player = player();
    pos = [player~'dimension', ceil(pos:0 / 512) - 1, ceil(pos:2 / 512) - 1];

    for(plots:'plots', plot = _; for(plot:'positions', if(_ == pos,
        print(player, format('r Current region is part of ', 't ' + plot:'name', 'r , owned by ', 't ' + plot:'owners', 'r , with ', 't ' + length(plot:'positions'), 'r  regions total'));
        return()
    )))
);

plot_join(name) ->
(
    plots = read_file('plots', 'json');
    player = player();

    for(plots:'plots', if(_:'name' == name,
        for(_:'owners', if(_ == player,
            print(player, format('t ' + player, 'l  already owns plot ', 't ' + name));
            return();
        ));
        _:'owners':length(_:'owners') = player();
        write_file('plots', 'json', plots);
        print(player, format('t ' + player, 'r  added to plot ', 't ' + name));
    ));
    print(player, format('l Plot ', 't ' + name, 'l  does not exist'))
);

plot_delete(name) ->
(
    plots = read_file('plots', 'json');
    player = player();

    i = 0;
    for(plots:'plots',
        if(_:'name' == name,
            delete(plots:'plots', i);
            write_file('plots', 'json', plots);
            print(player, format('r ' + 'plot ', 't ' + name, 'r  deleted'));
            for(_:'positions', unhighlight_region(_));
            return();
        );
        i += 1
    );
    print(player, format('l Plot ', 't ' + name, 'l  does not exist'))
);

plot_list() ->
(
    plots = read_file('plots', 'json');
    player = player();

    l = length(plots:'plots');
    if(l == 0,
        print(player, format('l No plots configured'));
        return()
    );

    print(player, '');
    print(player, format('mb List of plots'));
    for(plots:'plots',
        print(player, format('r - ', 't ' + _:'name', 'r  owned by ', 't ' + _:'owners', 'r  with regions ') + plot_tp_list(_:'name'))
    )
);

plot_tp_list(name) ->
(
    plots = read_file('plots', 'json');

    for(plots:'plots', if(_:'name' == name,
        string = '';
        i = 0;
        for(_:'positions',
            if(i > 0, string = string + format('r , '));
            dim = 't ';
            if(_:0 == 'the_nether', dim = 'n ');
            if(_:0 == 'the_end', dim = 'd ');
            string = string + format(dim + [_:1, _:2], '^ TP', '!/execute in ' + _:0 + ' run tp @s ' + _:1 * 512 + 256 + '.0 1 ' + _:2 * 512 + 256 + '.0 180 0');
            i = 1
        );
        return(string)
    ));
    print(player('all'), 'AMOG')
    // print(player, format('l Plot ', 't ' + name, 'l  does not exist'))
);

plot_tp(name) ->
(
    plots = read_file('plots', 'json');
    player = player();

    for(plots:'plots', if(_:'name' == name,
        tp = _:'tp';
        if(isInt(tp:1), tp:1 += '.0');
        if(isInt(tp:3), tp:3 += '.0');
        run('execute in ' + tp:0 + ' run tp ' + player~'name' + ' ' + tp:1 + ' ' + tp:2 + ' ' + tp:3 + ' ' + tp:4 + ' ' + tp:5);
        return()
    ));
    print(player, format('l Plot ', 't ' + name, 'l  does not exist'))
);

plot_modify(name, property, value) ->
(
    plots = read_file('plots', 'json');
    player = player();

    for(plots:'plots', if(_:'name' == name,
        plot = _;
        if(property == 'tp',
            for(plot:'positions', if(value:0 == _:0 && ceil(value:1 / 512) - 1 == _:1 && ceil(value:3 / 512) - 1 == _:2,
                plot:'tp' = value;
                write_file('plots', 'json', plots);
                print(player, format('r Teleport location for ', 't ' + name, 'r  set to ', 't ' + value));
                return()
            ));
            print(player, format('l Teleport location is outside claimed area for ', 't ' + name));
            return()
        );
        if(property == 'name',
            for(plots:'plots', if(_:'name' == value,
                print(player, format('l Name ', 't ' + value, 'l  is already taken'));
                return()
            ));
            plot:'name' = value;
            write_file('plots', 'json', plots);
            print(player, format('l Plot name changed from ', 't ' + name, 'l  to ', 't ' + value));
            return()
        )
    ));
    print(player, format('l Plot ', 't ' + name, 'l  does not exist'))
);

vanilla_fill(pos1, pos2, block) ->
(
    run('fill ' + pos1:0 + ' ' + pos1:1 + ' ' + pos1:2 + ' ' + pos2:0 + ' ' + pos2:1 + ' ' + pos2:2 + ' ' + block)
);

setblock(pos, block) ->
(
    run('setblock ' + pos:0 + ' ' + pos:1 + ' ' + pos:2 + ' ' + block)
);

isInt(n) -> return(n - floor(n) == 0);

highlight_region(pos, color) ->
(
    if(color == 'none', return());
    in_dimension(pos:0,
        pos = [pos:1 * 512, 0, pos:2 * 512];
        block = color + '_stained_glass';

        if(pos:0 >= 0,
            add_chunk_ticket(pos + [256, 0, 256], 'teleport', 16);
            schedule(1, _(outer(pos), outer(block)) ->
            (
                vanilla_fill(pos, pos + [511, 0, 0], block);
                vanilla_fill(pos + [511, 0, 0], pos + [511, 0, 511], block);
                vanilla_fill(pos + [511, 0, 511], pos + [0, 0, 511], block);
                vanilla_fill(pos + [0, 0, 511], pos, block);
            ));
            return()
        );

        if(pos:2 >= 0,
            add_chunk_ticket(pos + [256, 0, 256], 'teleport', 16);

            schedule(1, _(outer(pos), outer(block)) ->
                c_for(i = 0, i <= 496, i += 16,
                    setblock(pos + [i, 0, 0], block);
                    setblock(pos + [i + 15, 0, 0], block);

                    setblock(pos + [0, 0, i], block);
                    setblock(pos + [0, 0, i + 15], block);

                    setblock(pos + [i, 0, 511], block);
                    setblock(pos + [i + 15, 0, 511], block);

                    setblock(pos + [511, 0, i], block);
                    setblock(pos + [511, 0, i + 15], block);
                )
            )
        )
    )
);

unhighlight_region(pos) ->
(
    if(pos:1 >= 0, highlight_region(pos, 'blue'); return());

    if(pos:2 >= 0, in_dimension(pos:0,
        pos = [pos:1 * 512, 0, pos:2 * 512];

        add_chunk_ticket(pos + [256, 0, 256], 'teleport', 16);
        schedule(1, _(outer(pos), outer(block)) ->
            c_for(i = 0, i <= 496, i += 16,
                setblock(pos + [i, 0, 0], 'blue_stained_glass');
                setblock(pos + [i + 15, 0, 0], 'blue_stained_glass');

                setblock(pos + [0, 0, i], 'blue_stained_glass');
                setblock(pos + [0, 0, i + 15], 'blue_stained_glass');

                setblock(pos + [i, 0, 511], 'blue_stained_glass');
                setblock(pos + [i + 15, 0, 511], 'blue_stained_glass');

                setblock(pos + [511, 0, i], 'blue_stained_glass');
                setblock(pos + [511, 0, i + 15], 'blue_stained_glass');
            )
        )
    ))
)